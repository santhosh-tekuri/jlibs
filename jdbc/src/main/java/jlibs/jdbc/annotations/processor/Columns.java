/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.jdbc.annotations.processor;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.Noun;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.DAO;
import jlibs.jdbc.JavaType;
import jlibs.jdbc.SQLType;
import jlibs.jdbc.annotations.Column;
import jlibs.jdbc.annotations.Table;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
class Columns extends ArrayList<ColumnProperty>{
    public static Map<TypeElement, Columns> ALL = new HashMap<TypeElement, Columns>();
    
    public final String tableName;
    public final TypeElement tableClass;

    public Columns(TypeElement clazz){
        ALL.put(clazz, this);
        tableClass = clazz;
        String tableName = ModelUtil.getAnnotationValue(clazz, Table.class, "name");
        if(tableName.length()==0)
            tableName = Noun.pluralize(StringUtil.underscore(clazz.getSimpleName().toString()));
        this.tableName = tableName;
    }
    
    public ColumnProperty findByProperty(String propertyName){
        for(ColumnProperty prop: this){
            if(prop.propertyName().equals(propertyName))
                return prop;
        }
        return null;
    }

    public ColumnProperty findByColumn(String columnName){
        for(ColumnProperty prop: this){
            if(prop.columnName().equals(columnName))
                return prop;
        }
        return null;
    }

    public String columnName(String propertyName){
        ColumnProperty column = findByProperty(propertyName);
        return column!=null ? column.columnName() : null;
    }

    public String propertyName(String columnName){
        ColumnProperty column = findByColumn(columnName);
        return column!=null ? column.propertyName() : null;
    }

    public int autoColumn = -1;
    
    @Override
    public boolean add(ColumnProperty columnProperty){
        ColumnProperty clash = findByProperty(columnProperty.propertyName());
        if(clash!=null)
            throw new AnnotationError(columnProperty.element, columnProperty.annotation, "this property is already used by: "+clash.element);
        clash = findByColumn(columnProperty.columnName());
        if(clash!=null)
            throw new AnnotationError(columnProperty.element, columnProperty.annotation, "this column is already used by: "+clash.element);

        if(columnProperty.auto()){
            if(autoColumn!=-1)
                throw new AnnotationError(columnProperty.element, columnProperty.annotation, "two auto columns found: "+get(autoColumn).propertyName()+", "+columnProperty.propertyName());
            autoColumn = size();
        }

        columnProperty.validateType();

        columnProperty.reference = Reference.find(columnProperty.element);
        return super.add(columnProperty);
    }

    public void process(TypeElement c){
        for(ExecutableElement method: ElementFilter.methodsIn(c.getEnclosedElements())){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(method, Column.class);
            if(mirror!=null){
                if(!ModelUtil.isAccessible(method, true, false))
                    throw new AnnotationError(method, "Invalid access modifier used on method with @Column");
                add(new MethodColumnProperty(method, mirror));
            }
        }
        for(VariableElement element: ElementFilter.fieldsIn(c.getEnclosedElements())){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(element, Column.class);
            if(mirror!=null){
                if(!ModelUtil.isAccessible(element, true, false))
                    throw new AnnotationError(element, "Invalid access modifier used on field with @Column");
                add(new FieldColumnProperty(element, mirror));
            }
        }
    }

    public void generateConstructor(Printer printer){
        printer.printlns(
            "public "+printer.generatedClazz+"(DataSource dataSource){",
                PLUS,
                "super(dataSource, new TableMetaData(\""+StringUtil.toLiteral(tableName, false)+"\",",
                    PLUS
        );
        int i = 0;
        for(ColumnProperty column: this){
            printer.println(
                    "new ColumnMetaData(" ,
                    "\""+StringUtil.toLiteral(column.propertyName(), false)+"\", ",
                    "\""+StringUtil.toLiteral(column.columnName(), false)+"\", ",
                    JavaType.class.getSimpleName()+'.'+column.javaType().name()+", ",
                    SQLType.class.getSimpleName()+'.'+column.sqlType().name()+", ",
                    column.primary()+", ",
                    column.auto()+")",
                    (i==size()-1 ? "" : ",")
            );
            i++;
        }
        printer.printlns(
                    MINUS,
                "));",
                MINUS,
            "}"
        );
    }

    private void addDefaultCase(Printer printer){
        printer.printlns(
                "default:",
                    PLUS,
                    "throw new ImpossibleException();",
                    MINUS,
                    MINUS,
                "}",
             MINUS,
            "}"
        );
    }

    public void generateGetColumnValue(Printer printer){
        printer.printlns(
            "@Override",
            "public Object getColumnValue(int i, "+printer.clazz.getSimpleName()+" record){",
                PLUS,
                "Object value;",
                "switch(i){",
                    PLUS
        );
        int i = 0;
        for(ColumnProperty column: this){
            String propertyCode = column.toNativeTypeCode(column.getPropertyCode("record"));
            printer.printlns(
                "case "+i+":",
                    PLUS,
                    "value = "+propertyCode+';',
                    "return value==null ? "+SQLType.class.getSimpleName()+'.'+column.sqlType().name()+" : value;",
                    MINUS
            );
            i++;
        }
        addDefaultCase(printer);
    }

    public void generateSetColumnValue(Printer printer){
        printer.printlns(
            "@Override",
            "public void setColumnValue(int i, "+printer.clazz.getSimpleName()+" record, Object value){",
                PLUS,
                "switch(i){",
                    PLUS
        );
        int i = 0;
        for(ColumnProperty column: this){
            String value = "value";
            if(column.typeMapper()!=null)
                value = '('+ModelUtil.toString(column.javaTypeMirror(), true)+')'+value;
            value = column.toUserTypeCode(value);            
            printer.printlns(
                "case "+i+":",
                    PLUS,
                    column.setPropertyCode("record", value)+';',
                    "break;",
                    MINUS
            );
            i++;
        }
        addDefaultCase(printer);
    }

    public void generateTypeMapperConstants(Printer printer){
        for(ColumnProperty column: this){
            TypeMirror typeMapper = column.typeMapper();
            if(typeMapper!=null){
                String typeMapperQName = ModelUtil.toString(typeMapper, false);
                String constant = StringUtil.underscore(column.propertyName()).toUpperCase();
                printer.println("public static final "+typeMapperQName+" TYPE_MAPPER_"+constant+" = new "+typeMapperQName+"();");
            }
        }
    }

    private void generateNewRow(Printer printer){
        printer.printlns(
            "@Override",
            "public "+printer.clazz.getSimpleName()+" newRow(){",
                PLUS,
                "return new "+printer.clazz.getSimpleName()+"();",
                MINUS,
            "}"
        );
    }

    private void generateNewRecord(Printer printer){
        printer.printlns(
            "@Override",
            "public "+printer.clazz.getSimpleName()+" newRecord(ResultSet rs) throws SQLException{",
                PLUS,
                printer.clazz.getSimpleName()+" __record = newRow();"
        );
        int i = 1;
        for(ColumnProperty column: this){
            String code[] = column.getValueFromResultSet(i);
            if(code.length>1)
                printer.println(code[0]);
            String value = code[code.length-1];
            printer.println("setColumnValue("+(i-1)+", __record, "+value+");");
            i++;
        }
        printer.printlns(
                "return __record;",
                MINUS,
            "}"
        );
    }

    private void generateGetAutoColumnValue(Printer printer){
        printer.printlns(
            "@Override",
            "public Object getAutoColumnValue(ResultSet rs) throws SQLException{",
                PLUS
        );
        if(autoColumn==-1)
            printer.println("throw new "+ ImpossibleException.class.getName()+"();");
        else{
            ColumnProperty column = get(autoColumn);
            String code[] = column.getValueFromResultSet(1);
            if(code.length>1)
                printer.println(code[0]);
            printer.println("return "+code[code.length-1]+';');
        }
        printer.printlns(
                MINUS,
            "}"
        );
    }

    private void generateDAO(Printer printer){
        TypeElement extendClass = (TypeElement)((DeclaredType)ModelUtil.getAnnotationValue(printer.clazz, Table.class, "extend")).asElement();

        printer.printPackage();

        printer.importClass(ImpossibleException.class);
        printer.importPackage(DAO.class);
        printer.importPackage(Connection.class);
        printer.importClass(DataSource.class);
        if(ModelUtil.isInnerClass(extendClass))
            printer.importClass(extendClass);
        printer.println();

        printer.printClassDoc();
        printer.print("public class "+printer.generatedClazz +" extends "+extendClass.getQualifiedName());
        if(extendClass.getQualifiedName().contentEquals(DAO.class.getName()))
            printer.println("<"+printer.clazz.getSimpleName()+'>');
        printer.println("{");
        printer.indent++;

        generateTypeMapperConstants(printer);
        printer.println();
        generateConstructor(printer);
        printer.println();
        generateNewRow(printer);
        printer.println();
        generateNewRecord(printer);
        printer.println();
        generateGetAutoColumnValue(printer);
        printer.println();

        generateGetColumnValue(printer);
        printer.println();
        generateSetColumnValue(printer);

        for(ExecutableElement method: ElementFilter.methodsIn(extendClass.getEnclosedElements())){
            DMLMethod dmlMethod = DMLMethod.create(printer, method, this);
            if(dmlMethod!=null)
                dmlMethod.generate();
        }

        printer.indent--;
        printer.println("}");
    }

    public void generateDAO(){
        Printer pw = null;
        try{
            pw = Printer.get(tableClass, Table.class, TableAnnotationProcessor.FORMAT);
            generateDAO(pw);
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }finally{
            if(pw!=null)
                pw.close();
        }
    }
}

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
import jlibs.core.annotation.processing.AnnotationProcessor;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.DAO;
import jlibs.jdbc.JavaType;
import jlibs.jdbc.SQLType;
import jlibs.jdbc.annotations.Column;
import jlibs.jdbc.annotations.Table;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.util.Set;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
@SupportedAnnotationTypes("jlibs.jdbc.annotations.Table")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TableAnnotationProcessor extends AnnotationProcessor{
    private static final String SUFFIX = "DAO";
    public static final String FORMAT = "${package}._${class}"+SUFFIX;

    private Columns columns;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        for(TypeElement annotation: annotations){
            for(Element elem: roundEnv.getElementsAnnotatedWith(annotation)){
                try{
                    TypeElement c = (TypeElement)elem;
                    columns = new Columns();

                    columns.tableName = ModelUtil.getAnnotationValue(c, Table.class, "name");
                    if(columns.tableName.length()==0)
                        columns.tableName = StringUtil.underscore(c.getSimpleName().toString());

                    while(c!=null && !c.getQualifiedName().contentEquals(Object.class.getName())){
                        process(c);
                        c = ModelUtil.getSuper(c);
                    }
                    c = (TypeElement)elem;

                    if(columns.size()==0)
                        throw new AnnotationError(c, "Class with @Table must have atleast one column-property");

                    Printer pw = null;
                    try{
                        pw = Printer.get(c, Table.class, FORMAT);
                        generateClass(pw);
                    }catch(IOException ex){
                        throw new RuntimeException(ex);
                    }finally{
                        if(pw!=null)
                            pw.close();
                    }
                }catch(AnnotationError error){
                    error.report();
                }
            }
        }
        return true;
    }

    private void process(TypeElement c){
        for(ExecutableElement method: ElementFilter.methodsIn(c.getEnclosedElements())){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(method, Column.class);
            if(mirror!=null){
                if(!ModelUtil.isAccessible(method, true, false))
                    throw new AnnotationError(method, "Invalid access modifier used on method with @Column");
                columns.add(new MethodColumnProperty(method, mirror));
            }
        }
        for(VariableElement element: ElementFilter.fieldsIn(c.getEnclosedElements())){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(element, Column.class);
            if(mirror!=null){
                if(!ModelUtil.isAccessible(element, true, false))
                    throw new AnnotationError(element, "Invalid access modifier used on field with @Column");
                columns.add(new FieldColumnProperty(element, mirror));
            }
        }
    }

    private void generateClass(Printer printer){
        printer.printPackage();

        printer.importClass(ImpossibleException.class);
        printer.importPackage(DAO.class);
        printer.importPackage(Connection.class);
        printer.importClass(DataSource.class);
        printer.println();

        printer.printClassDoc();
        TypeElement extendClass = (TypeElement)((DeclaredType)ModelUtil.getAnnotationValue(printer.clazz, Table.class, "extend")).asElement();
        printer.print("public class "+printer.generatedClazz +" extends "+extendClass.getQualifiedName());
        if(extendClass.getQualifiedName().contentEquals(DAO.class.getName()))
            printer.println("<"+printer.clazz.getSimpleName()+'>');
        printer.println("{");
        printer.indent++;

        generateConstructor(printer);
        printer.println();
        generateNewRow(printer);
        printer.println();
        generateNewRecord(printer);
        printer.println();
        generateGetAutoColumnValue(printer);
        printer.println();

        columns.generateGetColumnValue(printer);
        printer.println();
        columns.generateSetColumnValue(printer);

        for(ExecutableElement method: ElementFilter.methodsIn(extendClass.getEnclosedElements())){
            DMLMethod dmlMethod = DMLMethod.create(printer, method, columns);
            if(dmlMethod!=null)
                dmlMethod.generate();
        }
        
        printer.indent--;
        printer.println("}");
    }

    private void generateConstructor(Printer printer){
        printer.printlns(
            "public "+printer.generatedClazz+"(DataSource dataSource){",
                PLUS,
                "super(dataSource, new TableMetaData(\""+StringUtil.toLiteral(columns.tableName, false)+"\",",
                    PLUS
        );
        int i = 0;
        for(ColumnProperty column: columns){
            printer.println(
                    "new ColumnMetaData(" ,
                    "\""+StringUtil.toLiteral(column.propertyName(), false)+"\", ",
                    "\""+StringUtil.toLiteral(column.columnName(), false)+"\", ",
                    JavaType.class.getSimpleName()+'.'+column.javaType().name()+", ",
                    SQLType.class.getSimpleName()+'.'+column.sqlType().name()+", ",
                    column.primary()+", ",
                    column.auto()+")",
                    (i==columns.size()-1 ? "" : ",")
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
        for(ColumnProperty column: columns){
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
        if(columns.autoColumn==-1)
            printer.println("throw new "+ImpossibleException.class.getName()+"();");
        else{
            ColumnProperty column = columns.get(columns.autoColumn);
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
}

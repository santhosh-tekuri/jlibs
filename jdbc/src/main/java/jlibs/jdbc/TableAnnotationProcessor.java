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

package jlibs.jdbc;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.AnnotationProcessor;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.graph.Visitor;
import jlibs.core.lang.BeanUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.annotations.*;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

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

    private Columns columns = new Columns();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        for(TypeElement annotation: annotations){
            for(Element elem: roundEnv.getElementsAnnotatedWith(annotation)){
                try{
                    columns.clear();
                    
                    TypeElement c = (TypeElement)elem;
                    while(c!=null && !c.getQualifiedName().contentEquals(Object.class.getName())){
                        process(c);
                        c = ModelUtil.getSuper(c);
                    }
                    c = (TypeElement)elem;

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
            if(mirror!=null)
                columns.add(new MethodColumnProperty(method, mirror));
        }
    }

    private void generateClass(Printer printer){
        printer.printPackage();

        printer.importClass(ImpossibleException.class);
        printer.importClass(DAO.class);
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

        columns.generateGetColumnValue(printer);
        printer.println();
        columns.generateSetColumnValue(printer);

        Class queryAnnotations[] = { Select.class, Insert.class, Update.class, Upsert.class, Delete.class };
        for(ExecutableElement method: ElementFilter.methodsIn(extendClass.getEnclosedElements())){
            Class queryAnnotation = null;
            AnnotationMirror mirror = null;
            for(Class annotation: queryAnnotations){
                mirror = ModelUtil.getAnnotationMirror(method, annotation);
                if(mirror!=null){
                    queryAnnotation = annotation;
                    break;
                }
            }

            if(queryAnnotation==Select.class)
                generateSelectMethod(printer, method);
            else if(queryAnnotation==Insert.class)
                generateInsertMethod(printer, method);
            else if(queryAnnotation==Update.class)
                generateUpdateMethod(printer, method);
            else if(queryAnnotation==Upsert.class)
                generateUpsertMethod(printer, method);
            else if(queryAnnotation==Delete.class)
                generateDeleteMethod(printer, method);
        }
        
        printer.indent--;
        printer.println("}");
    }

    private void generateConstructor(Printer printer){
        String tableName = ModelUtil.getAnnotationValue(printer.clazz, Table.class, "value");
        printer.printlns(
            "public "+printer.generatedClazz+"(DataSource dataSource){",
                PLUS,
                "super(dataSource, new TableMetaData(\""+StringUtil.toLiteral(tableName, false)+"\",",
                    PLUS
        );
        int i = 0;
        for(ColumnProperty column: columns){
            printer.println("new ColumnMetaData(\""+StringUtil.toLiteral(column.columnName(), false)+"\", "+column.primary()+')'+(i==columns.size()-1 ? "" : ","));
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

    private StringBuilder join(boolean useColumnName, ExecutableElement method, Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        StringBuilder buff = new StringBuilder();
        int i = 0;
        for(VariableElement param : method.getParameters()){
            String paramName = param.getSimpleName().toString();
            String propertyName = propertyVisitor==null ? paramName : propertyVisitor.visit(paramName);
            if(propertyName!=null){
                ColumnProperty column = columns.findByProperty(propertyName);
                if(column==null)
                    throw new AnnotationError(method, "invalid column property: "+paramName+"->"+propertyName);
                if(column.propertyType()!=param.asType())
                    throw new AnnotationError(param, paramName+" must be of type "+ModelUtil.toString(column.propertyType(), true));
                
                String item =  useColumnName ? column.columnName() : paramName;
                String value = visitor == null ? item : visitor.visit(item);
                if(value!=null){
                    if(i>0)
                        buff.append(separator);
                    buff.append(value);
                    i++;
                }
            }
        }
        return buff;
    }
    
    private StringBuilder columns(ExecutableElement method, Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        return join(true, method, propertyVisitor, visitor, separator);
    }

    private StringBuilder parameters(ExecutableElement method, Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        return join(false, method, propertyVisitor, visitor, separator);
    }

    private void generateDMLMethod(Printer printer, ExecutableElement method, String... code){
        printer.printlns(
            "",
            "@Override",
            ModelUtil.signature(method, true)+"{",
                PLUS
        );

        boolean noException = method.getThrownTypes().size() == 0;
        if(noException){
            printer.printlns(
                "try{",
                    PLUS
            );
        }
        printer.printlns(code);
        if(noException){
            printer.printlns(
                    MINUS,
                "}catch(java.sql.SQLException ex){",
                    PLUS,
                    "throw new "+JDBCException.class.getName()+"(ex);",
                    MINUS,
                "}"
            );
        }
        printer.printlns(
                MINUS,
            "}"
        );
    }

    private static final Visitor<String, String> ASSIGN_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String columnName){
            return columnName+"=?";
        }
    };

    private static final Visitor<String, String> SET_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String paramName){
            return paramName.startsWith("where") ? null : paramName;
        }
    };
    
    private static final Visitor<String, String> WHERE_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String paramName){
            if(paramName.startsWith("where")){
                paramName = paramName.substring("where".length());
                switch(paramName.charAt(0)){
                    case '_':
                    case '$':
                        return paramName.substring(1);
                    default:
                        return paramName;
                }
            }else
                return null;
        }
    };

    private static final Visitor<String, String> SET_WHERE_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String paramName){
            String propertyName = SET_VISITOR.visit(paramName);
            return propertyName==null ? WHERE_VISITOR.visit(paramName) : propertyName;
        }
    };

    private void generateSelectMethod(Printer printer, ExecutableElement method){
        if(method.getParameters().size()==0)
            throw new AnnotationError(method, "method with @Select annotation should take atleast one argument");

        StringBuilder where = columns(method, null, ASSIGN_VISITOR, " and ").insert(0, "where ");
        StringBuilder params = parameters(method, null, null, ", ");
        String methodName = method.getReturnType()==printer.clazz.asType() ? "first" : "all";
        generateDMLMethod(printer, method, "return "+methodName+"(\""+StringUtil.toLiteral(where, false)+"\", "+params+");");
    }

    private String insertQuery(ExecutableElement method, Visitor<String, String> propertyVisitor){
        StringBuilder columns = columns(method, propertyVisitor, null, ", ").insert(0, "(").append(')');
        StringBuilder values = parameters(method, propertyVisitor, new Visitor<String, String>(){
            @Override
            public String visit(String elem){
                return "?";
            }
        }, ", ").insert(0, "values(").append(')');
        StringBuilder params = parameters(method, propertyVisitor, null, ", ");

        return "insert(\""+StringUtil.toLiteral(columns+" "+values, false)+"\", "+params+')';
    }

    private void generateInsertMethod(Printer printer, ExecutableElement method){
        if(method.getParameters().size()==0)
            throw new AnnotationError(method, "method with @Insert annotation should take atleast one argument");
        
        boolean noReturn = method.getReturnType().getKind()==TypeKind.VOID;
        generateDMLMethod(printer, method, (noReturn ? "" : "return ")+insertQuery(method, null)+';');
    }

    private String updateQuery(ExecutableElement method){
        StringBuilder set = columns(method, SET_VISITOR, ASSIGN_VISITOR, ", ").insert(0, "set ");
        StringBuilder where = columns(method, WHERE_VISITOR, ASSIGN_VISITOR, " and ").insert(0, "where ");
        StringBuilder params = parameters(method, SET_WHERE_VISITOR, null, ", ");
        return "update(\""+StringUtil.toLiteral(set+" "+where, false)+"\", "+params+')';
    }
    
    private void generateUpdateMethod(Printer printer, ExecutableElement method){
        if(method.getParameters().size()==0)
            throw new AnnotationError(method, "method with @Update annotation should take atleast one argument");

        boolean noReturn = method.getReturnType().getKind()==TypeKind.VOID;
        generateDMLMethod(printer, method, (noReturn ? "" : "return ")+updateQuery(method)+';');
    }

    private void generateUpsertMethod(Printer printer, ExecutableElement method){
        if(method.getParameters().size()==0)
            throw new AnnotationError(method, "method with @Upsert annotation should take atleast one argument");

        String insertQuery = insertQuery(method, SET_WHERE_VISITOR);

        List<String> code = new ArrayList<String>();
        code.add("int count = "+updateQuery(method)+';');
        if(method.getReturnType().getKind()==TypeKind.VOID){
            code.add("if(count==0)");
            code.add(PLUS);
            code.add(insertQuery +';');
            code.add(MINUS);
        }else
            code.add("return count==0 ? "+insertQuery+" : count;");
        generateDMLMethod(printer, method, code.toArray(new String[code.size()]));
    }

    private void generateDeleteMethod(Printer printer, ExecutableElement method){
        if(method.getParameters().size()==0)
            throw new AnnotationError(method, "method with @Delete annotation should take atleast one argument");
        
        StringBuilder where = columns(method, null, ASSIGN_VISITOR, " and ").insert(0, "where ");
        StringBuilder params = parameters(method, null, null, ", ");

        boolean noReturn = method.getReturnType().getKind()==TypeKind.VOID;
        generateDMLMethod(printer, method, (noReturn ? "" : "return ")+"delete(\""+StringUtil.toLiteral(where.toString(), false)+"\", "+params+");");
    }

    private static abstract class ColumnProperty<E extends Element>{
        public E element;
        public AnnotationMirror annotation;
        protected ColumnProperty(E element, AnnotationMirror annotation){
            this.element = element;
            this.annotation = annotation;
        }

        public String columnName(){
            return ModelUtil.getAnnotationValue((Element)element, annotation, "value");
        }

        public boolean primary(){
            return (Boolean)ModelUtil.getAnnotationValue((Element)element, annotation, "primary");
        }

        public abstract String propertyName();
        public abstract TypeMirror propertyType();
        public abstract String getPropertyCode(String object);
        public abstract String setPropertyCode(String object, String value);

        @Override
        public int hashCode(){
            return propertyName().hashCode();
        }

        @Override
        public boolean equals(Object that){
            return that instanceof ColumnProperty && ((ColumnProperty)that).propertyName().equals(this.propertyName());
        }
    }

    private static class MethodColumnProperty extends ColumnProperty<ExecutableElement>{
        protected MethodColumnProperty(ExecutableElement method, AnnotationMirror annotation){
            super(method, annotation);
        }

        @Override
        public String propertyName(){
            String methodName = element.getSimpleName().toString();
            methodName = methodName.substring(methodName.startsWith("get") ? 3 : 2);
            switch(methodName.length()){
                case 0:
                    return methodName;
                case 1:
                    return methodName.toLowerCase();
                default:
                    return Character.toLowerCase(methodName.charAt(0))+methodName.substring(1);
            }
        }

        @Override
        public TypeMirror propertyType(){
            return element.getReturnType();
        }

        @Override
        public String getPropertyCode(String object){
            return object+'.'+element.getSimpleName()+"()";
        }

        @Override
        public String setPropertyCode(String object, String value){
            String propertyType = ModelUtil.toString(propertyType(), true);
            return object+".set"+BeanUtil.firstLetterToUpperCase(propertyName())+"(("+propertyType+')'+value+')';
        }
    }

    private static class Columns extends ArrayList<ColumnProperty>{
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

        public String[] columnNames(){
            String columnNames[] = new String[size()];
            for(int i=0; i<columnNames.length; i++)
                columnNames[i] = get(i).columnName();
            return columnNames;
        }

        public String columnsDeclaration(){
            StringBuilder buff = new StringBuilder("new String[]{ ");
            int i = 0;
            for(String column: columnNames()){
                if(i>0)
                    buff.append(", ");
                buff.append('"').append(StringUtil.toLiteral(column, false)).append('"');
                i++;
            }
            buff.append(" }");
            return buff.toString();
        }

        public boolean[] primaries(){
            boolean primaries[] = new boolean[size()];
            for(int i=0; i<primaries.length; i++)
                primaries[i] = get(i).primary();
            return primaries;
        }

        public String primariesDeclaration(){
            StringBuilder buff = new StringBuilder("new boolean[]{ ");
            int i = 0;
            for(Boolean primary: primaries()){
                if(i>0)
                    buff.append(", ");
                buff.append(primary);
                i++;
            }
            buff.append(" }");
            return buff.toString();
        }

        @Override
        public boolean add(ColumnProperty columnProperty){
            ColumnProperty clash = findByProperty(columnProperty.propertyName());
            if(clash!=null)
                throw new AnnotationError(columnProperty.element, columnProperty.annotation, "this property is already used by: "+clash.element);
            clash = findByColumn(columnProperty.columnName());
            if(clash!=null)
                throw new AnnotationError(columnProperty.element, columnProperty.annotation, "this column is already used by: "+clash.element);
            return super.add(columnProperty);
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
                    "switch(i){",
                        PLUS
            );
            int i = 0;
            for(ColumnProperty column: this){
                printer.printlns(
                    "case "+i+":",
                        PLUS,
                        "return "+column.getPropertyCode("record")+';',
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
                printer.printlns(
                    "case "+i+":",
                        PLUS,
                        column.setPropertyCode("record", "value")+';',
                        "break;",
                        MINUS
                );
                i++;
            }
            addDefaultCase(printer);
        }
    }
}

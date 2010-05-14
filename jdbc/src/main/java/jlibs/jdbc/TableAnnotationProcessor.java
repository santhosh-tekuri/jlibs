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

    private Map<ExecutableElement, AnnotationMirror> methods = new HashMap<ExecutableElement, AnnotationMirror>();
    private Map<String, String> properties = new HashMap<String, String>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        for(TypeElement annotation: annotations){
            for(Element elem: roundEnv.getElementsAnnotatedWith(annotation)){
                try{
                    methods.clear();
                    properties.clear();
                    
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
            if(mirror!=null){
                methods.put(method, mirror);
                String columnName = ModelUtil.getAnnotationValue(method, mirror, "value");
                if(properties.put(propertyName(method), columnName)!=null)
                    throw new AnnotationError(method, mirror, "duplicate column: "+columnName);
            }
        }
    }

    public String propertyName(ExecutableElement method){
        String methodName = method.getSimpleName().toString();
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

    public TypeMirror propertyType(String propertyName){
        for(ExecutableElement method: methods.keySet()){
            if(propertyName(method).equals(propertyName))
                return method.getReturnType();
        }
        return null;
    }

    public Set<String> columnNames(){
        Set<String> columns = new LinkedHashSet<String>();
        for(Map.Entry<ExecutableElement, AnnotationMirror> entry: methods.entrySet()){
            String column = ModelUtil.getAnnotationValue(entry.getKey(), entry.getValue(), "value");
            if(!columns.add(column))
                throw new AnnotationError(entry.getKey(), "duplicate column: "+column);
        }
        return columns;
    }

    public List<Boolean> primaries(){
        List<Boolean> primaries = new ArrayList<Boolean>();
        for(Map.Entry<ExecutableElement, AnnotationMirror> entry: methods.entrySet()){
            Boolean primary = ModelUtil.getAnnotationValue(entry.getKey(), entry.getValue(), "primary");
            primaries.add(primary);
        }
        return primaries;
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

        generateGetColumnValue(printer, methods);
        printer.println();
        generateSetColumnValue(printer, methods);

        for(ExecutableElement method: ElementFilter.methodsIn(extendClass.getEnclosedElements())){
            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(method, Insert.class);
            if(mirror==null){
	            mirror = ModelUtil.getAnnotationMirror(method, Delete.class);
	            if(mirror==null){
	                mirror = ModelUtil.getAnnotationMirror(method, Update.class);
	                if(mirror==null){
                        mirror = ModelUtil.getAnnotationMirror(method, Upsert.class);
                        if(mirror!=null)
                            generateUpsertMethod(printer, method);
                    }else
	                    generateUpdateMethod(printer, method);
	            }else
	           	generateDeleteMethod(printer, method);
            }else
                generateInsertMethod(printer, method);
        }
        printer.indent--;
        printer.println("}");
    }

    private void generateConstructor(Printer printer){
        String tableName = ModelUtil.getAnnotationValue(printer.clazz, Table.class, "value");
        printer.printlns(
            "public "+printer.generatedClazz+"(DataSource dataSource){",
                PLUS,
                "super(dataSource, \""+tableName+"\", "+columnsArray()+", "+primariesArray()+");",
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

    private String columnsArray(){
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

    private String primariesArray(){
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
    
    private void generateGetColumnValue(Printer printer, Map<ExecutableElement, AnnotationMirror> methods){
        printer.printlns(
            "@Override",
            "public Object getColumnValue(int i, "+printer.clazz.getSimpleName()+" record){",
                PLUS,
                "switch(i){",
                    PLUS
        );
        int i = 0;
        for(ExecutableElement method: methods.keySet()){
            printer.printlns(
                "case "+i+":",
                    PLUS,
                    "return record."+method.getSimpleName()+"();",
                    MINUS
            );
            i++;
        }
        addDefaultCase(printer);
    }

    private void generateSetColumnValue(Printer printer, Map<ExecutableElement, AnnotationMirror> methods){
        printer.printlns(
            "@Override",
            "public void setColumnValue(int i, "+printer.clazz.getSimpleName()+" record, Object value){",
                PLUS,
                "switch(i){",
                    PLUS
        );
        int i = 0;
        for(ExecutableElement method: methods.keySet()){
            String propertyName = propertyName(method);
            String returnType = ModelUtil.toString(method.getReturnType(), true);
            printer.printlns(
                "case "+i+":",
                    PLUS,
                    "record.set"+ BeanUtil.firstLetterToUpperCase(propertyName)+"(("+returnType+")value);",
                    "break;",
                    MINUS
            );
            i++;
        }
        addDefaultCase(printer);
    }

    private StringBuilder columns(final ExecutableElement method, final Visitor<String, String> propertyVisitor, final Visitor<String, String> visitor, String separator){
        StringBuilder columns = new StringBuilder();
        int i = 0;
        for(VariableElement param : method.getParameters()){
            String paramName = param.getSimpleName().toString();
            String propertyName = propertyVisitor==null ? paramName : propertyVisitor.visit(paramName);
            if(propertyName!=null){
                TypeMirror columnType = propertyType(propertyName);
                if(columnType==null)
                    throw new AnnotationError(method, "invalid column property: "+paramName+"->"+propertyName);
                if(columnType!=param.asType())
                    throw new AnnotationError(param, paramName+" must be of type "+ModelUtil.toString(columnType, true));
                
                String columnName = properties.get(propertyName);
                if(columnName==null)
                    throw new AnnotationError(method, "invalid column property: "+propertyName);
                
                String value = visitor == null ? columnName : visitor.visit(columnName);
                if(value!=null){
                    if(i>0)
                        columns.append(separator);
                    columns.append(value);
                    i++;
                }
            }
        }
        return columns;
    }
    
    private StringBuilder parameters(ExecutableElement method, Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        StringBuilder params = new StringBuilder();
        int i = 0;
        for(VariableElement param : method.getParameters()){
            String paramName = param.getSimpleName().toString();
            String propertyName = propertyVisitor==null ? paramName : propertyVisitor.visit(paramName);
            if(propertyName!=null){
                TypeMirror columnType = propertyType(propertyName);
                if(columnType==null)
                    throw new AnnotationError(method, "invalid column property: "+paramName);
                if(columnType!=param.asType())
                    throw new AnnotationError(param, paramName+" must be of type "+ModelUtil.toString(columnType, true));
                String value = visitor == null ? paramName : visitor.visit(paramName);
                if(value!=null){
                    if(i>0)
                        params.append(separator);
                    params.append(value);
                    i++;
                }
            }
        }
        return params;
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
}

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
import jlibs.core.graph.Visitor;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.JDBCException;
import jlibs.jdbc.annotations.*;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
abstract class DMLMethod{
    protected Printer printer;
    protected ExecutableElement method;
    protected AnnotationMirror mirror;
    protected Columns columns;

    protected DMLMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        this.printer = printer;
        this.method = method;
        this.mirror = mirror;
        this.columns = columns;
    }

    public static DMLMethod create(Printer printer, ExecutableElement method, Columns columns){
        AnnotationMirror mirror = ModelUtil.getAnnotationMirror(method, Select.class);
        if(mirror!=null)
            return new SelectMethod(printer, method, mirror, columns);

        mirror = ModelUtil.getAnnotationMirror(method, Insert.class);
        if(mirror!=null)
            return new InsertMethod(printer, method, mirror, columns);

        mirror = ModelUtil.getAnnotationMirror(method, Update.class);
        if(mirror!=null)
            return new UpdateMethod(printer, method, mirror, columns);

        mirror = ModelUtil.getAnnotationMirror(method, Upsert.class);
        if(mirror!=null)
            return new UpsertMethod(printer, method, mirror, columns);

        mirror = ModelUtil.getAnnotationMirror(method, Delete.class);
        if(mirror!=null)
            return new DeleteMethod(printer, method, mirror, columns);

        return null;
    }

    protected abstract String[] code();

    public void generate(){
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
        printer.printlns(code());
        if(noException){
            printer.printlns(
                    MINUS,
                "}catch(java.sql.SQLException ex){",
                    PLUS,
                    "throw new "+ JDBCException.class.getName()+"(ex);",
                    MINUS,
                "}"
            );
        }
        printer.printlns(
                MINUS,
            "}"
        );
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/
    
    private StringBuilder join(boolean useColumnName, Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
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

    protected StringBuilder columns(Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        return join(true, propertyVisitor, visitor, separator);
    }

    protected StringBuilder parameters(Visitor<String, String> propertyVisitor, Visitor<String, String> visitor, String separator){
        return join(false, propertyVisitor, visitor, separator);
    }

    /*-------------------------------------------------[ Visitors ]---------------------------------------------------*/
    
    static final Visitor<String, String> ASSIGN_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String columnName){
            return columnName+"=?";
        }
    };

    static final Visitor<String, String> SET_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String paramName){
            return paramName.startsWith("where") ? null : paramName;
        }
    };    
}

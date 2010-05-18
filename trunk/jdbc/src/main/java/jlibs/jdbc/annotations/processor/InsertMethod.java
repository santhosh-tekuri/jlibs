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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Santhosh Kumar T
 */
class InsertMethod extends AbstractDMLMethod{
    protected InsertMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    @Override
    protected String[] code(){
        String[] insertCode = super.code();

        TypeMirror returnType = method.getReturnType();
        if(returnType.getKind()==TypeKind.VOID)
            return insertCode;
        else if(returnType==printer.clazz.asType()){
            if(columns.autoColumn==-1){
                StringBuilder condition = new StringBuilder();
                StringBuilder params = new StringBuilder();
                for(ColumnProperty column: columns){
                    if(column.primary()){
                        if(condition.length()>0){
                            condition.append(" and ");
                            params.append(',');
                        }
                        condition.append(column.columnName()).append("=?");
                        params.append(column.propertyName());
                    }
                }
                String selectQuery = queryMethod("first", condition.insert(0, "where "), params);
                return new String[]{
                    insertCode[0].substring("return ".length()),
                    "return "+selectQuery+';'
                };
            }else{
                ColumnProperty autoColumn = columns.get(columns.autoColumn);
                String generatedKey = insertCode[0].substring("return ".length(), insertCode[0].length()-1);
                String generatedKeyType = ModelUtil.toString(autoColumn.propertyType(), true);
                return new String[]{
                    generatedKeyType+" __generatedKey = ("+generatedKeyType+')'+generatedKey+';',    
                    "return first(\"where "+autoColumn.columnName()+"=?\", __generatedKey);"
                };
            }
        }else
            throw new AnnotationError(method, "invalid return type");
    }

    @Override
    protected CharSequence[] defaultSQL(){
        return defaultSQL(null);
    }

    CharSequence[] defaultSQL(Visitor<String, String> propertyVisitor){
        StringBuilder columns = columns(propertyVisitor, null, ", ").insert(0, "(").append(')');
        StringBuilder values = parameters(propertyVisitor, new Visitor<String, String>(){
            @Override
            public String visit(String elem){
                return "?";
            }
        }, ", ").insert(0, "values(").append(')');

        return new CharSequence[]{
            columns.append(' ').append(values),
            parameters(propertyVisitor, null, ", ")
        };
    }

    @Override
    protected String userSQL(){
        return "";
    }
}

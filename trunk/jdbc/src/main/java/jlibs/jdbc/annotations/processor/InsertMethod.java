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
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
class InsertMethod extends DMLMethod{
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
            String insertQuery = insertCode[0].substring("return ".length(), insertCode[0].length()-1);
            return selectSQL(insertQuery);
        }else
            throw new AnnotationError(method, "invalid return type");
    }

    @Override
    protected CharSequence[] defaultSQL(){
        List<String> columnNames = new ArrayList<String>();
        List<String> columnValues = new ArrayList<String>();
        List<String> params = new ArrayList<String>();
        for(VariableElement param : method.getParameters()){
            ColumnProperty column = getColumn(param);

            columnNames.add("\"+"+column.columnName(true)+"+\"");
            columnValues.add("?");
            params.add(column.toNativeTypeCode(column.propertyName()));
        }
        StringBuilder sql = new StringBuilder("(")
                                    .append(StringUtil.join(columnNames.iterator(), ", "))
                                    .append(") VALUES(")
                                    .append(StringUtil.join(columnValues.iterator(), ", "))
                                    .append(')');
        return new CharSequence[]{
            sql,
            StringUtil.join(params.iterator(), ", ")
        };
    }

    private String[] selectSQL(String insertQuery){
        if(columns.autoColumn==-1){
            List<String> where = new ArrayList<String>();
            List<String> params = new ArrayList<String>();
            for(ColumnProperty column: columns){
                if(column.primary()){
                    if(ModelUtil.getParameter(method, column.propertyName())==null)
                        throw new AnnotationError(method, "column property '"+column.propertyName()+"' is missing in arguments.");
                    where.add("\"+"+column.columnName(true)+"+\"=?");
                    params.add(column.propertyName());
                }
            }
            StringBuilder sql = new StringBuilder(" WHERE ").append(StringUtil.join(where.iterator(), " AND "));
            return new String[]{
                insertQuery+';',
                "return "+queryMethod("first", sql, StringUtil.join(params.iterator(), ", "))+';'
            };
        }else{
            ColumnProperty autoColumn = columns.get(columns.autoColumn);
            String generatedKeyType = ModelUtil.toString(autoColumn.propertyType(), true);
            return new String[]{
                generatedKeyType+" __generatedKey = ("+generatedKeyType+')'+insertQuery+';',
                "return first(\"WHERE \"+"+autoColumn.columnName(true)+"+\"=?\", __generatedKey);"
            };
        }
    }
}

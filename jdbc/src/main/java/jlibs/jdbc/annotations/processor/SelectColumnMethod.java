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
import jlibs.core.util.CollectionUtil;
import jlibs.jdbc.IncorrectResultSizeException;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
// @enhancement allow to return single/listOf column values
public class SelectColumnMethod extends WhereMethod{
    protected SelectColumnMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    protected void validateParamCount(){}
    
    private ColumnProperty getColumn(){
        String columnProp = ModelUtil.getAnnotationValue(method, mirror, "column");
        if(columnProp.length()>0){
            ColumnProperty column = columns.findByProperty(columnProp);
            if(column==null)
                throw new AnnotationError(method, "invalid column property: "+columnProp);
            return column;
        }else{
            ColumnProperty column = new ColumnProperty<ExecutableElement>(method, mirror){
                @Override
                public String columnName(){
                    String value = ModelUtil.getAnnotationValue(method, mirror, "expression");
                    return replacePropertiesWithColumns(value); 
                }

                @Override
                public String propertyName(){
                    return "__value";
                }

                @Override
                public TypeMirror propertyType(){
                    TypeMirror returnType = method.getReturnType();
                    String s = ModelUtil.toString(returnType, false);
                    if(s.startsWith("java.util.List<"))
                        return ((List<? extends TypeMirror>)((DeclaredType)returnType).getTypeArguments()).get(0);
                    else if(s.equals("java.util.List"))
                        throw new AnnotationError(method, "the type of elements in returning List must be specified using Generics.");
                    return returnType;
                }

                @Override
                public String getPropertyCode(String object){
                    throw new UnsupportedOperationException();
                }

                @Override
                public String setPropertyCode(String object, String value){
                    throw new UnsupportedOperationException();
                }

                @Override
                protected AnnotationMirror typeMapperMirror(){
                    return null;
                }
            };
            column.validateType();
            return column;
        }
    }

    @Override
    protected String[] code(){
        ColumnProperty column = getColumn();

        int assertMinmumCount = (Integer)ModelUtil.getAnnotationValue(method, mirror, "assertMinimumCount");

        String columnType = ModelUtil.toString(column.propertyType(), true);
        CharSequence[] sequences = sql();
        String sql = String.format("SELECT %s FROM %s %s", column.columnName(), columns.tableName, sequences[0]);
        List<String> code = new ArrayList<String>();
        String methodName = methodName();
        CollectionUtil.addAll(code,
            String.format("jdbc.select%s(\"%s\", new RowMapper<%s>(){", methodName, StringUtil.toLiteral(sql, true), columnType),
                PLUS,
                String.format("public %s newRecord(ResultSet rs) throws SQLException{", columnType),
                    PLUS
        );

        if(methodName.equals("First") && assertMinmumCount!=-1)
            code.add("__found[0] = true;");
        String rowMapperCode[] = column.getValueFromResultSet(1);
        if(rowMapperCode.length>1)
            code.add(rowMapperCode[0]);
        code.add("return "+column.toUserTypeCode(rowMapperCode[rowMapperCode.length-1])+';');
        
        CollectionUtil.addAll(code,
                    MINUS,
                "}",
                MINUS,
            '}'+ (sequences[1].length()>0 ? String.format(", %s", sequences[1]) : "") + ");"
        );

        if(assertMinmumCount!=-1){
            String pojoName = ((DeclaredType)printer.clazz.asType()).asElement().getSimpleName().toString();
            if(methodName.equals("First")){
                code.set(0, columnType+" __result = "+code.get(0));
                code.add(0, "final boolean __found[] = { false };");
                CollectionUtil.addAll(code,
                    "if(!__found[0])",
                        PLUS,
                        "throw new "+ IncorrectResultSizeException.class.getSimpleName()+"(\""+pojoName+"\", 1, 0);",
                        MINUS,
                    "return __result;"
                );
            }else{
                code.set(0, "java.util.List<"+columnType+"> __result = "+code.get(0));
                CollectionUtil.addAll(code,
                    "if(__result.size()<"+assertMinmumCount+")",
                        PLUS,
                        "throw new "+ IncorrectResultSizeException.class.getSimpleName()+"(\""+pojoName+"\", "+assertMinmumCount+", __result.size());",
                        MINUS,
                    "return __result;"
                );
            }
        }else
            code.set(0, "return "+code.get(0));
        return code.toArray(new String[code.size()]);
    }

    protected String methodName(){
        String returnType = ModelUtil.toString(method.getReturnType(), true);
        ColumnProperty column = getColumn();
        String singleType = ModelUtil.toString(column.propertyType(), true);
        String listType = "java.util.List<"+singleType+">";

        if(singleType.equals(returnType))
            return "First";
        else if(listType.equals(returnType))
            return "All";
        else
            throw new AnnotationError(method, "return value must be of type "+singleType+" or "+listType);
    }
}
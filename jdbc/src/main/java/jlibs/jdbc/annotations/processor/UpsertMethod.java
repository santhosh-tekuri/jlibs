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
import jlibs.core.lang.NotImplementedException;
import jlibs.core.lang.StringUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
public class UpsertMethod extends UpdateMethod{
    protected UpsertMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);

        // @enhancement allow to return pojo
        if(method.getReturnType().getKind()!= TypeKind.VOID)
            throw new AnnotationError("method with @Upsert annotation should return void");
    }

    @Override
    protected String[] code(){
        if(method.getParameters().size()==0)
            throw new AnnotationError(method, "method with @Upsert annotation should take atleast one argument");

        String updateCode = queryMethod("update", defaultSQL());
        String insertCode = queryMethod("insert", insertSQL());

        List<String> code = new ArrayList<String>();
        code.add("int count = "+updateCode+';');
        code.add("if(count==0)");
        code.add(PLUS);
        code.add(insertCode+';');
        code.add(MINUS);

        TypeMirror returnType = method.getReturnType();
        if(returnType==printer.clazz.asType()){
            throw new NotImplementedException("Upsert Method returning Model Object");
        }else{
            switch(returnType.getKind()){
                case INT:
                    code.add("return count;");
                    break;
                case VOID:
                    break;
                default:
                    throw new AnnotationError(method, "unsupported return type");
            }
        }            

        return code.toArray(new String[code.size()]);
    }

    private CharSequence[] insertSQL(){
        List<String> columnNames = new ArrayList<String>();
        List<String> columnValues = new ArrayList<String>();
        List<String> params = new ArrayList<String>();
        for(VariableElement param : method.getParameters()){
            String paramName = param.getSimpleName().toString();
            if(paramName.indexOf('_')==-1){
                ColumnProperty column = getColumn(param);
                columnNames.add("\"+"+column.columnName(true)+"+\"");
                columnValues.add("?");
                params.add(column.toNativeTypeCode(paramName));
            }else{
                int underscore = paramName.indexOf('_');
                String hint = paramName.substring(0, underscore);
                String propertyName = paramName.substring(underscore+1);
                ColumnProperty column = getColumn(param, propertyName);

                if("where".equals(hint) || "is".equals(hint)){
                    columnNames.add("\"+"+column.columnName(true)+"+\"");
                    columnValues.add("?");
                    params.add(column.toNativeTypeCode(paramName));
                }else
                    throw new AnnotationError(param, "invalid hint: "+hint);
            }
        }
        StringBuilder query = new StringBuilder("(")
                                    .append(StringUtil.join(columnNames.iterator(), ", "))
                                    .append(") VALUES(")
                                    .append(StringUtil.join(columnValues.iterator(), ", "))
                                    .append(')');
        return new CharSequence[]{
            query,
            StringUtil.join(params.iterator(), ", ")
        };
    }
}

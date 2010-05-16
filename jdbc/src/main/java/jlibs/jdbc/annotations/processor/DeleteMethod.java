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
import jlibs.core.util.regex.TemplateMatcher;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

/**
 * @author Santhosh Kumar T
 */
public class DeleteMethod extends DMLMethod{
    protected DeleteMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    @Override
    protected String[] code(){
        boolean noReturn = method.getReturnType().getKind()== TypeKind.VOID;
        CharSequence query;
        final StringBuilder params = new StringBuilder();

        String value = ModelUtil.getAnnotationValue(method, mirror, "value");
        if(value.length()==0){
            if(method.getParameters().size()==0)
                throw new AnnotationError(method, "method with @Delete annotation should take atleast one argument");
            
            query = columns(null, ASSIGN_VISITOR, " and ").insert(0, "where ");
            params.append(parameters(null, null, ", "));
        }else{
            TemplateMatcher matcher = new TemplateMatcher("#{", "}");
            value = matcher.replace(value, new TemplateMatcher.VariableResolver(){
                @Override
                public String resolve(String propertyName){
                    String columnName = columns.columnName(propertyName);
                    if(columnName==null)
                        throw new AnnotationError(method, mirror, "unknown property: "+propertyName);
                    return columnName;
                }
            });
            matcher = new TemplateMatcher("${", "}");
            query = matcher.replace(value, new TemplateMatcher.VariableResolver(){
                @Override
                public String resolve(String paramName){
                    VariableElement param = ModelUtil.getParameter(method, paramName);
                    if(param==null)
                        throw new AnnotationError(method, mirror, "unknown parameter: "+paramName);
                    if(params.length()>0)
                        params.append(", ");
                    params.append(paramName);
                    return "?";
                }
            });
        }

        query = '"'+ StringUtil.toLiteral(query, false)+'"';
        String code = (noReturn ? "" : "return ")+"delete("+query;
        if(params.length()>0)
            code += ", "+params;
        code += ");";

        return new String[]{ code };
    }
}

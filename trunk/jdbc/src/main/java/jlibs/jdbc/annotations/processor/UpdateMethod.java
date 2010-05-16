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
import jlibs.core.lang.StringUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

/**
 * @author Santhosh Kumar T
 */
class UpdateMethod extends DMLMethod{
    protected UpdateMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    String queryMethod(){
        StringBuilder set = columns(SET_VISITOR, ASSIGN_VISITOR, ", ").insert(0, "set ");
        StringBuilder where = columns(WHERE_VISITOR, ASSIGN_VISITOR, " and ").insert(0, "where ");
        StringBuilder params = parameters(SET_WHERE_VISITOR, null, ", ");
        return "update(\""+ StringUtil.toLiteral(set+" "+where, false)+"\", "+params+')';
    }

    @Override
    protected String[] code(){
        if(method.getParameters().size()==0)
            throw new AnnotationError(method, "method with @Update annotation should take atleast one argument");

        boolean noReturn = method.getReturnType().getKind()== TypeKind.VOID;
        return new String[]{ (noReturn ? "" : "return ")+queryMethod()+';' };
    }

    /*-------------------------------------------------[ Visitors ]---------------------------------------------------*/

    static final Visitor<String, String> WHERE_VISITOR = new Visitor<String, String>(){
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

    static final Visitor<String, String> SET_WHERE_VISITOR = new Visitor<String, String>(){
        @Override
        public String visit(String paramName){
            String propertyName = SET_VISITOR.visit(paramName);
            return propertyName==null ? WHERE_VISITOR.visit(paramName) : propertyName;
        }
    };
}

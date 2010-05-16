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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

/**
 * @author Santhosh Kumar T
 */
public class SelectMethod extends DMLMethod{
    protected SelectMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    @Override
    protected String[] code(){
        if(method.getParameters().size()==0)
            throw new AnnotationError(method, "method with @Select annotation should take atleast one argument");

        StringBuilder where = columns(null, ASSIGN_VISITOR, " and ").insert(0, "where ");
        StringBuilder params = parameters(null, null, ", ");
        String methodName = method.getReturnType()==printer.clazz.asType() ? "first" : "all";

        return new String[]{ "return "+methodName+"(\""+ StringUtil.toLiteral(where, false)+"\", "+params+");" };
    }
}

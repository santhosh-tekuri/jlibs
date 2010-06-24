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

import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.IncorrectResultSizeException;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
// @enhancement allow to return single/listOf column values
public class SelectMethod extends WhereMethod{
    protected SelectMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    @Override
    protected String[] code() {
        String code[] = super.code();
        int assertMinmumCount = (Integer)ModelUtil.getAnnotationValue(method, mirror, "assertMinmumCount");
        if(assertMinmumCount==-1)
            return code;
        else{
            String pojoClass = ModelUtil.toString(printer.clazz.asType(), true);
            String pojoName = ((DeclaredType)printer.clazz.asType()).asElement().getSimpleName().toString();
            if(methodName().equals("first")){
                return new String[]{
                    pojoClass+" __pojo = "+code[0].substring("return ".length()),
                    "if(__pojo==null)",
                    PLUS,
                    "throw new "+ IncorrectResultSizeException.class.getSimpleName()+"(\""+pojoName+"\", 1, 0);",
                    MINUS,
                    "return __pojo;"    
                };
            }else{
                return new String[]{
                    "java.util.List<"+pojoClass+"> __pojos = "+code[0].substring("return ".length()),
                    "if(__pojos.size()<"+assertMinmumCount+")",
                    PLUS,
                    "throw new "+ IncorrectResultSizeException.class.getSimpleName()+"(\""+pojoName+"\", "+assertMinmumCount+", __pojos.size());",
                    MINUS,
                    "return __pojos;"
                };
            }
        }            
    }

    protected String methodName(){
        return method.getReturnType()==printer.clazz.asType() ? "first" : "all";
    }
}

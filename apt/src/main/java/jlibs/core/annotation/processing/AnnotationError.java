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

package jlibs.core.annotation.processing;

import jlibs.core.lang.model.ModelUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * @author Santhosh Kumar T
 */
public class AnnotationError extends Error{
    private Element pos1;
    private AnnotationMirror pos2;
    private AnnotationValue pos3;

    public AnnotationError(String message){
        super(message);
    }

    public AnnotationError(Element pos, String message){
        this(message);
        this.pos1 = pos;
    }

    public AnnotationError(Element elem, Class annotation, String message){
        this(elem, ModelUtil.getAnnotationMirror(elem, annotation), message);
    }

    public AnnotationError(Element elem, Class annotation, String method, String message){
        this(elem, ModelUtil.getAnnotationMirror(elem, annotation),
                ModelUtil.getRawAnnotationValue(elem, ModelUtil.getAnnotationMirror(elem, annotation), method),
                message);
    }

    public AnnotationError(Element pos1, AnnotationMirror pos2, String message){
        this(pos1, message);
        this.pos2 = pos2;
    }

    public AnnotationError(Element pos1, AnnotationMirror pos2, AnnotationValue pos3, String message){
        this(pos1, pos2, message);
        this.pos3 = pos3;
    }

    public void printMessage(Diagnostic.Kind kind){
        if(pos1==null)
            Environment.get().getMessager().printMessage(kind, getMessage());
        else if(pos2==null)
            Environment.get().getMessager().printMessage(kind, getMessage(), pos1);
        else if(pos3==null)
            Environment.get().getMessager().printMessage(kind, getMessage(), pos1, pos2);
        else
            Environment.get().getMessager().printMessage(kind, getMessage(), pos1, pos2, pos3);
    }

    public void report(){
        printMessage(Diagnostic.Kind.ERROR);
    }

    public void warn(){
        printMessage(Diagnostic.Kind.WARNING);
    }
}

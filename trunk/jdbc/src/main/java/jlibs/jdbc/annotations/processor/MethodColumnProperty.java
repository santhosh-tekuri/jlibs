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
import jlibs.core.lang.BeanUtil;
import jlibs.core.lang.model.ModelUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Santhosh Kumar T
 */
class MethodColumnProperty extends ColumnProperty<ExecutableElement>{
    private String propertyName;
    private TypeMirror propertyType;

    protected MethodColumnProperty(ExecutableElement method, AnnotationMirror annotation){
        super(method, annotation);
        String methodName = element.getSimpleName().toString();
        if(methodName.startsWith("get")){
            methodName = methodName.substring(3);
            propertyType = method.getReturnType();
        }else if(methodName.startsWith("is")){
            methodName = methodName.substring(2);
            propertyType = method.getReturnType();
        }else if(methodName.startsWith("set")){
            methodName = methodName.substring(3);
            propertyType = method.getParameters().get(0).asType();
        }else
            throw new AnnotationError(element, "@Column annotation cann't be applied to this method");

        switch(methodName.length()){
            case 0:
                propertyName = methodName;
                break;
            case 1:
                propertyName = methodName.toLowerCase();
                break;
            default:
                propertyName = Character.toLowerCase(methodName.charAt(0))+methodName.substring(1);
        }
    }

    @Override
    public String propertyName(){
        return propertyName;
    }

    @Override
    public TypeMirror propertyType(){
        return propertyType;
    }

    @Override
    public String getPropertyCode(String object){
        String prefix = propertyType.getKind() == TypeKind.BOOLEAN ? "is" : "get";
        return object+'.'+prefix+BeanUtil.firstLetterToUpperCase(propertyName())+"()";
    }

    @Override
    public String setPropertyCode(String object, String value){
        String propertyType = ModelUtil.toString(propertyType(), true);
        return object+".set"+BeanUtil.firstLetterToUpperCase(propertyName())+"(("+propertyType+')'+value+')';
    }
}

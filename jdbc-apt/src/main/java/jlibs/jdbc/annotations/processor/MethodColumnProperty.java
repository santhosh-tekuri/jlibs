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

import static jlibs.core.lang.BeanUtil.*;

/**
 * @author Santhosh Kumar T
 */
class MethodColumnProperty extends ColumnProperty<ExecutableElement>{
    private String propertyName;
    private TypeMirror propertyType;

    protected MethodColumnProperty(ExecutableElement method, AnnotationMirror annotation){
        super(method, annotation);
        String methodName = element.getSimpleName().toString();
        if(methodName.startsWith(SET))
            propertyType = method.getParameters().get(0).asType();
        else
            propertyType = method.getReturnType();

        try{
            propertyName = getPropertyName(methodName);
        }catch(IllegalArgumentException ex){
            throw new AnnotationError(element, "@Column annotation can't be applied to this method");
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
        String prefix = propertyType.getKind()==TypeKind.BOOLEAN ? IS : GET;
        return object+'.'+prefix+ BeanUtil.getMethodSuffix(propertyName())+"()";
    }

    @Override
    public String setPropertyCode(String object, String value){
        String propertyType = ModelUtil.toString(propertyType(), true);
        return object+'.'+SET+getMethodSuffix(propertyName())+"(("+propertyType+')'+value+')';
    }
}

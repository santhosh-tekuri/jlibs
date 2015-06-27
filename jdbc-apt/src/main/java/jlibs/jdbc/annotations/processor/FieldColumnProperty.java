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

import jlibs.core.lang.model.ModelUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Santhosh Kumar T
 */
class FieldColumnProperty extends ColumnProperty<VariableElement>{
    protected FieldColumnProperty(VariableElement method, AnnotationMirror annotation){
        super(method, annotation);
    }

    @Override
    public String propertyName(){
        return element.getSimpleName().toString();
    }

    @Override
    public TypeMirror propertyType(){
        return element.asType();
    }

    @Override
    public String getPropertyCode(String object){
        return object+'.'+propertyName();
    }

    @Override
    public String setPropertyCode(String object, String value){
        String propertyType = ModelUtil.toString(propertyType(), true);
        return object+'.'+propertyName()+" = ("+propertyType+')'+value;
    }
}
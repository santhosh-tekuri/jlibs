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

import jlibs.core.lang.ArrayUtil;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * @author Santhosh Kumar T
 */
// @todo ensure that column-property is accessible to the DAO generated
abstract class ColumnProperty<E extends Element>{
    public E element;
    public AnnotationMirror annotation;
    protected ColumnProperty(E element, AnnotationMirror annotation){
        this.element = element;
        this.annotation = annotation;
    }

    public String columnName(){
        String columnName = ModelUtil.getAnnotationValue((Element) element, annotation, "name");
        return columnName.length()==0 ? StringUtil.underscore(propertyName()) : columnName;
    }

    public boolean primary(){
        return (Boolean)ModelUtil.getAnnotationValue((Element)element, annotation, "primary");
    }

    public boolean auto(){
        return (Boolean)ModelUtil.getAnnotationValue((Element)element, annotation, "auto");
    }

    public String resultSetType(){
        TypeMirror propertyType = propertyType();
        boolean primitive = ModelUtil.isPrimitive(propertyType);
        boolean primitiveWrapper = ModelUtil.isPrimitiveWrapper(propertyType);

        if(primitive)
            return ModelUtil.toString(propertyType, false);
        else if(primitiveWrapper){
            String type = ModelUtil.toString(propertyType, false);
            return ModelUtil.primitives[ArrayUtil.indexOf(ModelUtil.primitiveWrappers, type)];
        }else
            return ModelUtil.toString(propertyType, false);
    }

    public abstract String propertyName();
    public abstract TypeMirror propertyType();
    public abstract String getPropertyCode(String object);
    public abstract String setPropertyCode(String object, String value);

    @Override
    public int hashCode(){
        return propertyName().hashCode();
    }

    @Override
    public boolean equals(Object that){
        return that instanceof ColumnProperty && ((ColumnProperty)that).propertyName().equals(this.propertyName());
    }
}

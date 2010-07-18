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
import jlibs.core.lang.ArrayUtil;
import jlibs.core.lang.ClassUtil;
import jlibs.core.lang.StringUtil;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.JavaType;
import jlibs.jdbc.SQLType;
import jlibs.jdbc.annotations.TypeMapper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Santhosh Kumar T
 */
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

    protected AnnotationMirror typeMapperMirror(){
        return ModelUtil.getAnnotationMirror(element, TypeMapper.class);
    }
    
    public TypeMirror javaTypeMirror(){
        AnnotationMirror typeMapperMirror = typeMapperMirror();

        if(typeMapperMirror==null)
            return propertyType();
        else
            return (DeclaredType)ModelUtil.getAnnotationValue(element, typeMapperMirror, "mapsTo");
    }

    public String resultSetType(){
        TypeMirror propertyType = javaTypeMirror();
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

    public JavaType javaType(){
        try{
            Class propertyType = Class.forName(ModelUtil.toString(javaTypeMirror(), true));
            propertyType = ClassUtil.unbox(propertyType);
            return JavaType.valueOf(propertyType);
        }catch(ClassNotFoundException ex){
            return null;
        }
    }

    public SQLType sqlType(){
        return javaType().sqlTypes[0];
    }

    public TypeMirror typeMapper(){
        AnnotationMirror mirror = typeMapperMirror();
        return mirror==null ? null : (DeclaredType)ModelUtil.getAnnotationValue(element, mirror, "mapper");
    }
    
    public abstract String propertyName();
    public abstract TypeMirror propertyType();
    public abstract String getPropertyCode(String object);
    public abstract String setPropertyCode(String object, String value);

    public String[] getValueFromResultSet(int index){
        String resultSetType = resultSetType();
        int dot = resultSetType.lastIndexOf('.');
        if(dot!=-1)
            resultSetType = resultSetType.substring(dot+1);

        if(ModelUtil.isPrimitive(javaTypeMirror()) || ModelUtil.isPrimitiveWrapper(javaTypeMirror())){
            String name = propertyName();
            return new String[]{
                resultSetType+' '+name+" = rs.get"+StringUtil.capitalize(resultSetType)+'('+index+");",
                "rs.wasNull() ? null : "+name
            };
        }else
            return new String[]{ "rs.get"+StringUtil.capitalize(resultSetType)+'('+index+')' };
    }

    @Override
    public int hashCode(){
        return propertyName().hashCode();
    }

    @Override
    public boolean equals(Object that){
        return that instanceof ColumnProperty && ((ColumnProperty)that).propertyName().equals(this.propertyName());
    }

    // ensure that propertyType is valid javaType that can be fetched from ResultSet
    public void validateType(){
        if(javaType()==null)
            throw new AnnotationError(element, annotation, resultSetType()+" has no mapping SQL Type");
    }

    public String toNativeTypeCode(String value){
        TypeMirror typeMirror = typeMapper();
        if(typeMirror!=null)
            value = "TYPE_MAPPER_"+StringUtil.underscore(propertyName()).toUpperCase()+".userToNative("+value+")";
        return value;
    }

    public String toUserTypeCode(String value){
        TypeMirror typeMirror = typeMapper();
        if(typeMirror!=null)
            value = "TYPE_MAPPER_"+StringUtil.underscore(propertyName()).toUpperCase()+".nativeToUser("+value+")";
        return value;
    }
}

/*
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.lang.model;

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.Environment;
import jlibs.core.lang.BeanUtil;
import jlibs.core.lang.NotImplementedException;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class ModelUtil{
    @SuppressWarnings({"unchecked"})
    public static <T> T parent(Element element, Class<T> type){
        do{
            element = element.getEnclosingElement();
        }while(!type.isInstance(element));
        return (T)element;
    }

    public static TypeElement getSuper(TypeElement clazz){
        return (TypeElement)((DeclaredType)clazz.getSuperclass()).asElement();
    }

    public static String getPackage(TypeElement clazz){
        return ((PackageElement)clazz.getEnclosingElement()).getQualifiedName().toString();
    }

    public static String toString(TypeMirror mirror){
        switch(mirror.getKind()){
            case DECLARED:
                Name paramType = ((TypeElement)((DeclaredType)mirror).asElement()).getQualifiedName();
                return paramType.toString();
            case INT:
                return "java.lang.Integer";
            case BOOLEAN:
            case FLOAT:
            case DOUBLE:
            case LONG:
            case BYTE:
                return "java.lang."+ BeanUtil.firstLetterToUpperCase(mirror.getKind().toString().toLowerCase());
            case ARRAY:
                return toString(((ArrayType)mirror).getComponentType())+"[]";
            default:
                throw new NotImplementedException(mirror.getKind()+" is not implemented for "+mirror.getClass());
        }
    }

    /*-------------------------------------------------[ Annotation ]---------------------------------------------------*/

    public static boolean matches(AnnotationMirror mirror, Class annotation){
        return ((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName().contentEquals(annotation.getCanonicalName());
    }

    public static AnnotationMirror getAnnotationMirror(Element elem, Class annotation){
        for(AnnotationMirror mirror: elem.getAnnotationMirrors()){
            if(matches(mirror, annotation))
                return mirror;
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getAnnotationValue(Element pos, AnnotationMirror mirror, String method){
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : Environment.get().getElementUtils().getElementValuesWithDefaults(mirror).entrySet()){
            if(entry.getKey().getSimpleName().contentEquals(method))
                return (T)entry.getValue().getValue();
        }
        throw new AnnotationError(pos, mirror, "annotation "+((TypeElement)mirror.getAnnotationType().asElement()).getQualifiedName()+" is missing "+method);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAnnotationValue(Element elem, Class annotation, String method){
        AnnotationMirror mirror = getAnnotationMirror(elem, annotation);
        if(mirror!=null)
            return (T)getAnnotationValue(elem, mirror, method);
        else
            return null;
    }
}

/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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

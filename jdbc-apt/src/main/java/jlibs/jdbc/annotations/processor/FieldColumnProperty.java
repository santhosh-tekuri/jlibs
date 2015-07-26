/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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
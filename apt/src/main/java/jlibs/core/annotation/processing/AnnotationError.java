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

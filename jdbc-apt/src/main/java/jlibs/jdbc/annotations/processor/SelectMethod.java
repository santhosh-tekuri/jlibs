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

import jlibs.core.annotation.processing.AnnotationError;
import jlibs.core.annotation.processing.Printer;
import jlibs.core.lang.model.ModelUtil;
import jlibs.jdbc.IncorrectResultSizeException;
import jlibs.jdbc.paging.Paging;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import java.util.Collection;
import java.util.List;

import static jlibs.core.annotation.processing.Printer.MINUS;
import static jlibs.core.annotation.processing.Printer.PLUS;

/**
 * @author Santhosh Kumar T
 */
// @enhancement allow to return single/listOf column values
public class SelectMethod extends WhereMethod{
    protected SelectMethod(Printer printer, ExecutableElement method, AnnotationMirror mirror, Columns columns){
        super(printer, method, mirror, columns);
    }

    @Override
    protected String[] code() {
        String code[] = super.code();
        int assertMinmumCount = (Integer)ModelUtil.getAnnotationValue(method, mirror, "assertMinimumCount");
        if(assertMinmumCount==-1)
            return code;
        else{
            String pojoClass = ModelUtil.toString(printer.clazz.asType(), true);
            String pojoName = ((DeclaredType)printer.clazz.asType()).asElement().getSimpleName().toString();
            if(methodName().equals("first")){
                return new String[]{
                    pojoClass+" __pojo = "+code[0].substring("return ".length()),
                    "if(__pojo==null)",
                        PLUS,
                        "throw new "+ IncorrectResultSizeException.class.getSimpleName()+"(\""+pojoName+"\", 1, 0);",
                        MINUS,
                    "return __pojo;"    
                };
            }else{
                return new String[]{
                    "java.util.List<"+pojoClass+"> __pojos = "+code[0].substring("return ".length()),
                    "if(__pojos.size()<"+assertMinmumCount+")",
                        PLUS,
                        "throw new "+ IncorrectResultSizeException.class.getSimpleName()+"(\""+pojoName+"\", "+assertMinmumCount+", __pojos.size());",
                        MINUS,
                    "return __pojos;"
                };
            }
        }            
    }

    protected String methodName(){
        String returnType = ModelUtil.toString(method.getReturnType(), true);
        String singleType = ModelUtil.toString(printer.clazz.asType(), true);
        String listType = List.class.getName()+'<'+singleType+'>';
        String pagingType = Paging.class.getName()+'<'+singleType+'>';

        if(returnType.equals(singleType))
            return "first";
        else if(returnType.equals(listType))
            return "all";
        else if(returnType.equals(pagingType)){
            Collection<AnnotationValue> pageBys = ModelUtil.getAnnotationValue(method, mirror, "pageBy");
            if(pageBys.size()==0)
                throw new AnnotationError(method, mirror, "method returning "+pagingType+" must specify @Select.pageBy attribute");
            return "new "+pagingType+"(this, ";
        }else
            throw new AnnotationError(method, "return value must be of type "+singleType+" or "+listType);
    }
}

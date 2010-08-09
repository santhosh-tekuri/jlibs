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
import jlibs.core.annotation.processing.AnnotationProcessor;
import jlibs.core.lang.model.ModelUtil;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
@SupportedAnnotationTypes("jlibs.jdbc.annotations.Table")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TableAnnotationProcessor extends AnnotationProcessor{
    private static final String SUFFIX = "DAO";
    public static final String FORMAT = "${package}._${class}"+SUFFIX;

    @Override
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        try{
            for(TypeElement annotation: annotations){
                for(Element elem: roundEnv.getElementsAnnotatedWith(annotation)){
                    TypeElement c = (TypeElement)elem;
                    Columns columns = new Columns(c);

                    while(c!=null && !c.getQualifiedName().contentEquals(Object.class.getName())){
                        columns.process(c);
                        c = ModelUtil.getSuper(c);
                    }
                    c = (TypeElement)elem;

                    if(columns.size()==0)
                        throw new AnnotationError(c, "Class with @Table must have atleast one column-property");
                }
            }
            for(Columns columns: Columns.ALL.values())
                columns.generateDAO();
        }catch(AnnotationError error){
            error.report();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }finally{
            Columns.ALL.clear();
        }
        return true;
    }
}

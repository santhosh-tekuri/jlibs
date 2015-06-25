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
import jlibs.jdbc.annotations.References;
import jlibs.jdbc.annotations.Table;
import org.kohsuke.MetaInfServices;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
@SupportedAnnotationTypes({"jlibs.jdbc.annotations.Table", "jlibs.jdbc.annotations.Database"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@MetaInfServices(Processor.class)
public class TableAnnotationProcessor extends AnnotationProcessor{
    private static final String SUFFIX = "DAO";
    public static final String FORMAT = "${package}._${class}"+SUFFIX;

    @Override
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        try{
            for(TypeElement annotation: annotations){
                for(Element elem: roundEnv.getElementsAnnotatedWith(annotation)){
                    if(ModelUtil.getAnnotationMirror(elem, Table.class)!=null){
                        TypeElement c = (TypeElement)elem;
                        Columns columns = new Columns(c);

                        while(c!=null && !c.getQualifiedName().contentEquals(Object.class.getName())){
                            columns.process(c);
                            c = ModelUtil.getSuper(c);
                        }
                        c = (TypeElement)elem;

                        if(columns.size()==0)
                            throw new AnnotationError(c, "Class with @Table must have atleast one column-property");
                    }else
                        ConnectionInfo.add((PackageElement)elem);
                }
            }
            for(Columns columns: Columns.ALL.values()){
                ConnectionInfo conInfo = ConnectionInfo.get(columns);
                if(conInfo!=null)
                    conInfo.validate(columns);

                for(ColumnProperty column: columns){
                    if(column.reference!=null){
                        if(column.reference.column()==null){
                            AnnotationMirror mirror = ModelUtil.getAnnotationMirror(column.element, References.class);
                            AnnotationValue annotationValue = ModelUtil.getRawAnnotationValue(column.element, mirror, "column");
                            throw new AnnotationError(column.element, mirror, annotationValue,
                                    column.reference.tableClass.getSimpleName()+" doesn't has column property named "+column.reference.columnName);
                        }
                    }
                }
                columns.generateDAO();
            }
        }catch(AnnotationError error){
            error.report();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }finally{
            Columns.ALL.clear();
            ConnectionInfo.ALL.clear();
        }
        return true;
    }
}

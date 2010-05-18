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

package jlibs.core.annotation.processing;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

/**
 * @author Santhosh Kumar T
 */
public abstract class AnnotationProcessor extends AbstractProcessor{
    @Override
    public void init(ProcessingEnvironment processingEnv){
        super.init(processingEnv);
        Environment.set(processingEnv);
    }
    
    public void debug(String message){
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}

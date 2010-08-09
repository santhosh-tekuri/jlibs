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

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

/**
 * @author Santhosh Kumar T
 */
public final class Environment{
    private static final ThreadLocal<ProcessingEnvironment> ENV = new ThreadLocal<ProcessingEnvironment>();
    public static void set(ProcessingEnvironment env){
        ENV.set(env);
    }
    public static ProcessingEnvironment get(){
        return ENV.get();
    }

    public static void debug(String message){
        get().getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}

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

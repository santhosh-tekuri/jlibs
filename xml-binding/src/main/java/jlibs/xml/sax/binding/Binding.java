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

package jlibs.xml.sax.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Santhosh Kumar T
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Binding{
    public String value();
    
    /**
     * method signatures supported:
     *      void/C method(SAXContext current, Attributes attrs)
     *      void/C method(C current, Attributes attrs)
     *      void/C method(Attributes attrs)
     *      void/C method()
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Start{
        public String[] value() default "";
    }

    /**
     * method signatures supported:
     *      void/C method(SAXContext current, String text)
     *      void/C method(C current, String text)
     *      void/C method(String text)
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Text{
        public String[] value() default "";
    }

    /**
     * method signatures supported:
     *      void/C method(SAXContext current)
     *      void/C method(C current)
     *      void/C method()
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Finish{
        public String[] value() default "";
    }

    /**
     * method signature is not taken into account.
     * the method is used to hold the annotation, and is never called.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Element{
        public String element();
        public Class clazz();
    }
}

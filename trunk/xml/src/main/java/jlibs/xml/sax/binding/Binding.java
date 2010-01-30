/*
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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

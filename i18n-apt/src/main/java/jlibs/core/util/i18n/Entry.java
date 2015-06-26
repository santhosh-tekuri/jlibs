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

package jlibs.core.util.i18n;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Santhosh Kumar T
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Entry{
    /** translated to comment in properties file */
    String value() default "";

    /** key of the property */
    String lhs() default "";
    
    String hintName() default "";
    Hint hint() default Hint.NONE;

    /** value of the property */
    String rhs() default "";
}

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

package jlibs.jdbc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Santhosh Kumar T
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.SOURCE)
public @interface Column{
    /**
     * Name of the table column. If not specified it is computed from
     * the field/property name by converting it to lowercase and
     * inserting underscores between words
     */
    String name() default "";
    boolean primary() default false;
    boolean auto() default false;
    boolean nativeType() default false;
}

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

package jlibs.core.util.i18n;

import java.util.Locale;

/**
 * Simple holder class that associates a Locale instance with the current thread
 *
 * @author Santhosh Kumar Tekuri
 */
public class LocaleContext{
    private static final ThreadLocal<Locale> threadLocal = new InheritableThreadLocal<Locale>();

    public static Locale getLocale(){
        Locale locale = threadLocal.get();
        return locale==null ? Locale.getDefault() : locale;
    }

    public static void setLocale(Locale locale){
        threadLocal.set(locale);
    }

    public static void reset(){
        threadLocal.remove();
    }
}

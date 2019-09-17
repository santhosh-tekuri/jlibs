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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Santhosh Kumar T
 */
public class I18N{
    protected static final String FORMAT = "${package}._Bundle";
    public static final String BASENAME = "Bundle";

    @SuppressWarnings({"unchecked"})
    public static <T> T getImplementation(Class<T> bundleClass){
        try{
            String qname = FORMAT.replace("${package}", bundleClass.getPackage()!=null?bundleClass.getPackage().getName():"")
                                 .replace("${class}", bundleClass.getSimpleName());
            if(qname.startsWith(".")) // default package
                qname = qname.substring(1);
            Class implementation = bundleClass.getClassLoader().loadClass(qname);
            return (T)implementation.getDeclaredField("INSTANCE").get(null);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static ResourceBundle bundle(Class clazz){
        return ResourceBundle.getBundle(clazz.getPackage().getName().replace('.', '/')+"/"+BASENAME);
    }

    public static ResourceBundle bundle(Class clazz, Locale locale){
        return ResourceBundle.getBundle(clazz.getPackage().getName().replace('.', '/')+"/"+BASENAME, locale);
    }

    public static String getValue(Class clazz, String key, Object... args){
        try{
            return MessageFormat.format(bundle(clazz).getString(key), args);
        }catch(MissingResourceException ex){
            return null;
        }
    }

    public static String getValue(Class clazz, Locale locale, String key, Object... args){
        try{
            return MessageFormat.format(bundle(clazz, locale).getString(key), args);
        }catch(MissingResourceException ex){
            return null;
        }
    }

    public static String getHint(Class clazz, String member, String hint, Object... args){
        return getValue(clazz, clazz.getSimpleName()+'.'+member+'.'+hint, args);
    }

    public static String getHint(Class clazz, Locale locale, String member, String hint, Object... args){
        return getValue(clazz, locale, clazz.getSimpleName()+'.'+member+'.'+hint, args);
    }

    public static String getHint(Class clazz, String hint, Object... args){
        return getValue(clazz, clazz.getSimpleName()+'.'+hint, args);
    }

    public static String getHint(Class clazz, Locale locale, String hint, Object... args){
        return getValue(clazz, locale, clazz.getSimpleName()+'.'+hint, args);
    }
}

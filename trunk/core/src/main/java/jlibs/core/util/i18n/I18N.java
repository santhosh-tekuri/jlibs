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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import jlibs.core.lang.model.ModelUtil;

/**
 * @author Santhosh Kumar T
 */
public class I18N{
    public static final String BASENAME = "Bundle";
    
    @SuppressWarnings({"unchecked"})
    public static <T> T getImplementation(Class<T> bundleClass){
        try{
            return (T)ModelUtil.findClass(bundleClass, BundleAnnotationProcessor.FORMAT).getDeclaredField("INSTANCE").get(null);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static ResourceBundle bundle(Class clazz){
        return ResourceBundle.getBundle(clazz.getPackage().getName().replace('.', '/')+"/"+BASENAME);
    }

    public static String getValue(Class clazz, String key, Object... args){
        try{
            return MessageFormat.format(bundle(clazz).getString(key), args);
        }catch(MissingResourceException ex){
            return null;
        }
    }    

    public static String getHint(Class clazz, String member, String hint, Object... args){
        return getValue(clazz, clazz.getSimpleName()+'.'+member+'.'+hint, args);
    }

    public static String getHint(Class clazz, String hint, Object... args){
        return getValue(clazz, clazz.getSimpleName()+'.'+hint, args);
    }
}

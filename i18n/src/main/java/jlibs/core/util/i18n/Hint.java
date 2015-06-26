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

/**
 * @author Santhosh Kumar T
 */
public enum Hint{
    DISPLAY_NAME("displayName"),
    DESCRIPTION("description"),
    ADVANCED("advanced", "false"),
    NONE(null),
    ;

    private String key;
    private String defaultValue;

    private Hint(String key){
        this(key, null);
    }

    private Hint(String key, String defaultValue){
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String key(){
        return key;
    }

    public String defaultValue(){
        return defaultValue;
    }

    public String stringValue(Class clazz, String member){
        String value = I18N.getHint(clazz, member, key);
        return value==null ? defaultValue : value;
    }

    public String stringValue(Class clazz){
        String value = I18N.getHint(clazz, key);
        return value==null ? defaultValue : value;
    }

    public boolean booleanValue(Class clazz, String member){
        return Boolean.parseBoolean(stringValue(clazz, member));
    }

    public boolean booleanValue(Class clazz){
        return Boolean.parseBoolean(stringValue(clazz));
    }

    public int intValue(Class clazz, String member){
        return Integer.parseInt(stringValue(clazz, member));
    }

    public int intValue(Class clazz){
        return Integer.parseInt(stringValue(clazz));
    }
}

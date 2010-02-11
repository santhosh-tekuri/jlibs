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

package jlibs.core.lang;

/**
 * This class contains helper methods for working with
 * bitwise flags
 * 
 * @author Santhosh Kumar T
 */
public class Flag{
    /*-------------------------------------------------[ int ]---------------------------------------------------*/

    public static int set(int value, int flag){
        return value | flag;
    }

    public static boolean isSet(int value, int flag){
        return (value & flag)!=0;
    }

    public static int unset(int value, int flag){
        return value & ~flag;
    }

    public static int toggle(int value, int flag){
        return value ^ ~flag;
    }

    /*-------------------------------------------------[ long ]---------------------------------------------------*/

    public static long set(long value, long flag){
        return value | flag;
    }

    public static boolean isSet(long value, long flag){
        return (value & flag)!=0;
    }

    public static long unset(long value, long flag){
        return value & ~flag;
    }

    public static long toggle(long value, long flag){
        return value ^ ~flag;
    }
}

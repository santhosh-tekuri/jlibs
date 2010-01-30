/**
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

package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public enum OS{
    WINDOWS_NT("Windows NT"),
    WINDOWS_95("Windows 95"),
    WINDOWS_98("Windows 98"),
    WINDOWS_2000("Windows 2000"),
    // add new windows versions here
    WINDOWS_OTHER("Windows"),

    SOLARIS("Solaris"),
    LINUX("Linux"),
    HP_UX("HP-UX"),
    IBM_AIX("AIX"),
    SGI_IRIX("Irix"),
    SUN_OS("SunOS"),
    COMPAQ_TRU64_UNIX("Digital UNIX"),
    MAC("Mac OS X", "Darwin"),
    FREEBSD("freebsd"),
    // add new unix versions here

    OS2("OS/2"),
    COMPAQ_OPEN_VMS("OpenVMS"),
    OTHER("");

    private String names[];

    private OS(String... names){
        this.names = names;
    }

    public boolean isWindows(){
        return ordinal()<=WINDOWS_OTHER.ordinal();
    }
    
    public boolean isUnix(){
        return ordinal()>WINDOWS_OTHER.ordinal() && ordinal()<OS2.ordinal();
    }

    /*-------------------------------------------------[ Static Methods ]---------------------------------------------------*/

    public static OS get(String osName){
        osName = osName.toLowerCase();
        for(OS os: values()){
            for(String name: os.names){
                if(osName.contains(name.toLowerCase()))
                    return os;
            }
        }
        throw new ImpossibleException();
    }

    private static OS current;
    public static OS get(){
        if(current==null)
            current = get(System.getProperty("os.name"));
        return current;
    }

    public static void main(String[] args){
        System.out.println(OS.get().isUnix());
    }
}

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

import jlibs.core.io.FileUtil;
import jlibs.core.net.URLUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Santhosh Kumar T
 */
public class ClassUtil{
    /**
     * Returns the classpath resource(directory or jar) from which
     * specified class is loaded.
     */
    public static String getClassPath(Class clazz){
        URL url = clazz.getResource(clazz.getSimpleName() + ".class");
        if("jar".equals(url.getProtocol())){
            String path = url.getPath();
            try{
                return URLUtil.toSystemID(new URL(path.substring(0, path.lastIndexOf('!'))));
            }catch(MalformedURLException ex){
                throw new ImpossibleException("as per JAR URL Syntax this should never happen", ex);
            }
        }
        String resource = URLUtil.toSystemID(url);
        String relativePath = "/"+clazz.getName().replace(".", FileUtil.SEPARATOR)+".class";
        if(resource.endsWith(relativePath))
            resource = resource.substring(0, resource.length()-relativePath.length());
        return resource;
    }

    /**
     * Returns the classloader for the speficied clazz. Unlike
     * Class.getClassLoader() this will never return null.
     */
    public static ClassLoader getClassLoader(Class clazz){
        ClassLoader classLoader = clazz.getClassLoader();
        return classLoader==null ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    public static void main(String[] args){
        System.out.println(getClassPath(String.class));
        System.out.println(getClassPath(ClassUtil.class));
        System.out.println(getClassLoader(String.class));
    }
}

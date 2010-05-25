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

import jlibs.core.io.FileUtil;
import jlibs.core.net.URLUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.HashMap;

/**
 * @author Santhosh Kumar T
 */
public class ClassUtil{
    /**
     * Returns the classpath resource(directory or jar) from which
     * specified class is loaded.
     *
     * @param clazz class
     * @return resource from which the class is loaded
     */
    public static String getClassPath(Class clazz){
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        if(codeSource!=null)
            return URLUtil.toSystemID(codeSource.getLocation());
        else{
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
    }

    /**
     * Returns the classloader for the speficied <code>clazz</code>. Unlike
     * {@link Class#getClassLoader()} this will never return null.
     */
    public static ClassLoader getClassLoader(Class clazz){
        ClassLoader classLoader = clazz.getClassLoader();
        return classLoader==null ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    /*-------------------------------------------------[ PRIMITIVES ]---------------------------------------------------*/
    
    private static final HashMap<Class, Class> PRIMITIVES = new HashMap<Class, Class>();
    static{
        PRIMITIVES.put(Void.class,         void.class  );
        PRIMITIVES.put(Boolean.class,      boolean.class  );
        PRIMITIVES.put(Character.class,    char.class     );
        PRIMITIVES.put(Byte.class,         byte.class     );
        PRIMITIVES.put(Short.class,        short.class    );
        PRIMITIVES.put(Integer.class,      int.class      );
        PRIMITIVES.put(Long.class,         long.class     );
        PRIMITIVES.put(Float.class,        float.class    );
        PRIMITIVES.put(Double.class,       double.class   );
    }
    public static Class unbox(Class clazz){
        Class unboxedClass = PRIMITIVES.get(clazz);
        return unboxedClass==null ? clazz : unboxedClass;
    }
    public static boolean isPrimitiveWrapper(Class clazz){
        return PRIMITIVES.containsKey(clazz);
    }

    private static final HashMap<Class, Class> PRIMITIVE_WRAPPERS = new HashMap<Class, Class>();
    static{
        PRIMITIVE_WRAPPERS.put(void.class,    void.class  );
        PRIMITIVE_WRAPPERS.put(boolean.class, Boolean.class  );
        PRIMITIVE_WRAPPERS.put(char.class,    Character.class);
        PRIMITIVE_WRAPPERS.put(byte.class,    Byte.class     );
        PRIMITIVE_WRAPPERS.put(short.class,   Short.class    );
        PRIMITIVE_WRAPPERS.put(int.class,     Integer.class  );
        PRIMITIVE_WRAPPERS.put(long.class,    Long.class     );
        PRIMITIVE_WRAPPERS.put(float.class,   Float.class    );
        PRIMITIVE_WRAPPERS.put(double.class,  Double.class   );
    }
    public static Class box(Class clazz){
        Class boxedClass = PRIMITIVE_WRAPPERS.get(clazz);
        return boxedClass==null ? clazz : boxedClass;
    }

    public static Class getPrimitiveType(String className){
        if(className.equals("void"))
            return void.class;
        else if(className.equals("boolean"))
            return boolean.class;
        else if(className.equals("char"))
            return char.class;
        else if(className.equals("byte"))
            return byte.class;
        else if(className.equals("short"))
            return short.class;
        else if(className.equals("int"))
            return int.class;
        else if(className.equals("long"))
            return long.class;
        else if(className.equals("float"))
            return float.class;
        else if(className.equals("double"))
            return double.class;
        else
            return null;
    }

    /*-------------------------------------------------[ ClassContext ]---------------------------------------------------*/
    
    /**
     * A helper class to get the call context. It subclasses SecurityManager
     * to make getClassContext() accessible.
     */
    private static class ClassContext extends SecurityManager{
        public static final ClassContext INSTANCE = new ClassContext();

        @Override
        protected Class[] getClassContext(){
            return super.getClassContext();
        }
    }

    /**
     * returns the calling class.
     *
     * offset 0 returns class who is calling this method
     * offset 1 returns class who called your method
     * and so on
     */
    public static Class getClassingClass(int offset){
        Class[] context = ClassContext.INSTANCE.getClassContext();
        offset += 2;
        return context.length>offset ? context[offset] : null;
    }

    public static Class getClassingClass(){
        return getClassingClass(1);
    }

    public static ClassLoader getClassingClassLoader(){
        Class caller = getClassingClass(1);
        return caller==null ? ClassLoader.getSystemClassLoader() : getClassLoader(caller);
    }
}

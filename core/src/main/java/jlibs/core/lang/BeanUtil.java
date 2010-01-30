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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * This class contains helper methods for working with
 * javabeans
 *
 * @author Santhosh Kumar T
 */
public class BeanUtil{
    /*-------------------------------------------------[ Getter Method ]---------------------------------------------------*/

    private static final String GET = "get"; //NOI18N
    private static final String IS  = "is";  //NOI18N

    public static Method getGetterMethod(Class beanClass, String property){
        try{
            return beanClass.getMethod(GET+firstLetterToUpperCase(property));
        }catch(NoSuchMethodException ex1){
            try{
                return beanClass.getMethod(IS+firstLetterToUpperCase(property));
            }catch(NoSuchMethodException ex2){
                return null;
            }
        }
    }

    public static Method getGetterMethod(Class beanClass, String property, Class propertyType){
        String prefix = propertyType==boolean.class || propertyType==Boolean.class ? IS : GET;
        try{
            return beanClass.getMethod(prefix+firstLetterToUpperCase(property));
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    /*-------------------------------------------------[ Setter Method ]---------------------------------------------------*/

    public static Method getSetterMethod(Class beanClass, String property){
        Method getter = getGetterMethod(beanClass, property);
        if(getter==null)
            return null;
        else
            return getSetterMethod(beanClass, property, getter.getReturnType());
    }

    private static final String SET = "set"; //NOI18N

    public static Method getSetterMethod(Class beanClass, String property, Class propertyType){
        try{
            return beanClass.getMethod(SET+firstLetterToUpperCase(property), propertyType);
        } catch(NoSuchMethodException ex){
            return null;
        }
    }

    /*-------------------------------------------------[ Property ]---------------------------------------------------*/

    public static Class getPropertyType(Class beanClass, String property){
        Method getter = getGetterMethod(beanClass, property);
        if(getter==null)
            return null;
        else
            return getter.getReturnType();
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getProperty(Object bean, String property) throws InvocationTargetException{
        try{
            return (T)getGetterMethod(bean.getClass(), property).invoke(bean);
        }catch(IllegalAccessException ex){
            throw new ImpossibleException(ex); // because getter method is public
        }
    }

    public static void setProperty(Object bean, String property, Object value) throws InvocationTargetException{
        try{
            getSetterMethod(bean.getClass(), property).invoke(bean, value);
        }catch(IllegalAccessException ex){
            throw new ImpossibleException(ex); // because setter method is public
        }

    }

    /*-------------------------------------------------[ Helper ]---------------------------------------------------*/

    public static String firstLetterToUpperCase(String str){
        switch(str.length()){
            case 0:
                return str;
            case 1:
                return str.toUpperCase();
            default:
                return Character.toUpperCase(str.charAt(0))+str.substring(1);
        }
    }
}

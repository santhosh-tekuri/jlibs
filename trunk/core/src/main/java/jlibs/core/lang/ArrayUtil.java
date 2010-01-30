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

import jlibs.core.graph.Filter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class ArrayUtil{

    /*-------------------------------------------------[ Finding Item ]---------------------------------------------------*/

    /**
     * returns first index of <code>item<code> in given <code>array</code> starting
     * from <code>fromIndex</code>(inclusive)
     *
     * @param array     object array, can be null
     * @param item      item to be searched, can be null
     * @param fromIndex index(inclusive) from which search happens.
     * @return          -1 if array is null, or the item is not found
     *                  otherwize returns first index of item in array
     */
    public static <T, S extends T> int indexOf(T array[], S item, int fromIndex){
        if(array==null)
            return -1;

        for(int i=fromIndex; i<array.length; i++){
            if(Util.equals(array[i], item))
                return i;
        }
        return -1;
    }

    /**
     * returns first index of <code>item<code> in given <code>array</code>
     *
     * @param array     object array, can be null
     * @param item      item to be searched, can be null
     * @return          -1 if array is null, or the item is not found
     *                  otherwize returns first index of item in array
     */
    public static <T, S extends T> int indexOf(T array[], S item){
        return indexOf(array, item, 0);
    }

    /**
     * tells whether the <code>array</code> contains the given <code>item</code>
     *
     * @param array     object array, can be null
     * @param item      item to be searched, can be null
     * @return          false if array is null, or item is not found
     *                  otherwize returns true;
     */
    public static <T, S extends T> boolean contains(T array[], S item){
        return indexOf(array, item)!=-1;
    }

    /*-------------------------------------------------[ Boundary Items ]---------------------------------------------------*/

    /**
     * returns first element in given array.
     * if array is null or of length zero, returns null
     */
    public static <T> T getFirst(T array[]){
        return array!=null && array.length>0 ? array[0] : null;
    }

    /**
     * returns last element in given array.
     * if array is null or of length zero, returns null
     */
    public static <T> T getLast(T array[]){
        return array!=null && array.length>0 ? array[array.length-1] : null;
    }

    /*-------------------------------------------------[ Contents Equality ]---------------------------------------------------*/

    /**
     * returns array1[from1, from1+length-1] and array2[from2, from2+length-1] contain same elements
     * in case of index out of range, returns false
     */
    public static boolean equals(Object array1[], int from1, Object array2[], int from2, int length){
        if(from1<array1.length || from1+length>array1.length)
            return false;
        if(from2<array2.length || from2+length>array2.length)
            return false;
        for(int i=0; i<length; i++){
            if(!Util.equals(array1[from1+i], array2[from2+i]))
                return false;
        }
        return true;
    }

    /** Returns true if <code>array1</code> starts with <code>array2</code> */
    public static boolean startsWith(Object array1[], Object array2[]){
        return equals(array1, 0, array2, 0, array2.length);
    }

    /** Returns true if <code>array1</code> ends with <code>array2</code> */
    public static boolean endsWith(Object array1[], Object array2[]){
        return equals(array1, array1.length-array2.length, array2, 0, array2.length);
    }

    /*-------------------------------------------------[ Copy ]---------------------------------------------------*/

    /**
     * Copies all elements from <code>src</code> to <code>dest</code>
     * If dest.length<src.length, it copies only the number of elements
     * that can fit in dest array.
     *
     * @return the dest array spcified
     */
    @SuppressWarnings({"SuspiciousSystemArraycopy"})
    public static <T> T[] copy(Object src[], T dest[]){
        System.arraycopy(src, 0, dest, 0, Math.min(src.length, dest.length));
        return dest;
    }

    /**
     * returns the clone of given array <code>src</code>.
     *
     * @param componentType 	the componentType used for the new array created
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T[] clone(Object src[], Class<T> componentType){
        return copy(src, (T[])Array.newInstance(componentType, src.length));
    }

    /**
     * returns new array which contains only those elements from given array
     * which are selected by the filter
     */
    public static <T> T[] filter(T array[], Filter<T> filter){
        List<T> filteredList = new ArrayList<T>(array.length);
        for(T element: array){
            if(filter.select(element))
                filteredList.add(element);
        }
        @SuppressWarnings("unchecked")
        T filteredArray[] = (T[])Array.newInstance(array.getClass().getComponentType(), filteredList.size());
        return filteredList.toArray(filteredArray);
    }

    /*-------------------------------------------------[ Concat ]---------------------------------------------------*/

    @SuppressWarnings({"unchecked"})
    public static <T> T[] concat(Object[] array1, Object array2[], Class<T> componentType){
        Object newArray[] = (Object[])Array.newInstance(componentType, array1.length+array2.length);
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);
        return (T[])newArray;
    }

    /**
     * Returns new array which has all values from array1 and array2 in order.
     * The componentType for the new array is determined by the componentTypes of
     * two arrays.
     */
    public static Object[] concat(Object array1[], Object array2[]){
        Class<?> class1 = array1.getClass().getComponentType();
        Class<?> class2 = array2.getClass().getComponentType();
        Class<?> commonClass = class1.isAssignableFrom(class2)
                                    ? class1
                                    : (class2.isAssignableFrom(class1) ? class2 : Object.class);
        return concat(array1, array2, commonClass);
    }
}

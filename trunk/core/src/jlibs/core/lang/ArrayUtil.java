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
public class ArrayUtil{
    /**
     * returns first index of <code>item<code> in given <code>array</code>
     *
     * @param array     object array, can be null
     * @param item      item to be searched, can be null
     * @return          -1 if array is null, or the item is not found
     *                  otherwize returns first index of item in array
     */
    public static <T> int indexOf(T array[], T item){
        if(array==null)
            return -1;
        
        for(int i=0; i<array.length; i++){
            if(Util.equals(array[i], item))
                return i;
        }
        return -1;
    }

    /**
     * tells whether the <code>array</code> contains the given <code>item</code>
     *
     * @param array     object array, can be null
     * @param item      item to be searched, can be null
     * @return          false if array is null, or item is not found
     *                  otherwize returns true;
     */
    public static <T> boolean contains(T array[], T item){
        return indexOf(array, item)!=-1;
    }
}

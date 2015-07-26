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

package jlibs.core.lang;

import jlibs.core.graph.Filter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
     * Returns first index of {@code item} in given {@code array}
     *
     * @param array     object array, can be null
     * @param item      item to be searched, can be null
     * @return          -1 if array is null, or the item is not found.
     *                  Otherwise returns first index of item in array
     *
     * @see #indexOf(Object[], Object, int) 
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
     * Returns first element in given {@code array}.
     * <p>
     * if {@code array} is null or of length zero, then returns {@code null}
     */
    public static <T> T getFirst(T array[]){
        return array!=null && array.length>0 ? array[0] : null;
    }

    /**
     * Returns last element in given {@code array}.
     * <p>
     * if {@code array} is null or of length zero, then returns {@code null}
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
     * Copies all elements from {@code src} array to {@code dest} array.
     * <p>
     * If {@code dest.length&lt;src.length}, it copies only the number of elements
     * that can fit in {@code dest} array.
     *
     * @param src   source array
     * @param dest  destination array
     * 
     * @return the destination array specified
     */
    @SuppressWarnings({"SuspiciousSystemArraycopy"})
    public static <T> T[] copy(Object src[], T dest[]){
        System.arraycopy(src, 0, dest, 0, Math.min(src.length, dest.length));
        return dest;
    }

    /**
     * returns the clone of {@code src} array.
     *
     * @param src               source array
     * @param componentType 	the componentType used for the new array created
     *
     * @return cloned array
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T[] clone(Object src[], Class<T> componentType){
        return copy(src, (T[])Array.newInstance(componentType, src.length));
    }

    /**
     * Returns new array which contains only those elements from given {@code array}
     * which are selected by the {@code filter}
     *
     * @param array the array to be filtered
     * @param filter filter to use
     *
     * @return filtered array
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

    /*-------------------------------------------------[ toObjectArray ]---------------------------------------------------*/

    public static Boolean[] toObjectArray(boolean arr[]){
        if(arr==null)
            return null;
        
        Boolean result[] = new Boolean[arr.length];
        for(int i=arr.length-1; i>=0; i--)
            result[i] = arr[i];
        return result;
    }

    public static Character[] toObjectArray(char arr[]){
        if(arr==null)
            return null;

        Character result[] = new Character[arr.length];
        for(int i=arr.length-1; i>=0; i--)
            result[i] = arr[i];
        return result;
    }

    public static Byte[] toObjectArray(byte arr[]){
        if(arr==null)
            return null;

        Byte result[] = new Byte[arr.length];
        for(int i=arr.length-1; i>=0; i--)
            result[i] = arr[i];
        return result;
    }

    public static Short[] toObjectArray(short arr[]){
        if(arr==null)
            return null;

        Short result[] = new Short[arr.length];
        for(int i=arr.length-1; i>=0; i--)
            result[i] = arr[i];
        return result;
    }

    public static Integer[] toObjectArray(int arr[]){
        if(arr==null)
            return null;

        Integer result[] = new Integer[arr.length];
        for(int i=arr.length-1; i>=0; i--)
            result[i] = arr[i];
        return result;
    }

    public static Long[] toObjectArray(long arr[]){
        if(arr==null)
            return null;

        Long result[] = new Long[arr.length];
        for(int i=arr.length-1; i>=0; i--)
            result[i] = arr[i];
        return result;
    }

    public static Float[] toObjectArray(float arr[]){
        if(arr==null)
            return null;

        Float result[] = new Float[arr.length];
        for(int i=arr.length-1; i>=0; i--)
            result[i] = arr[i];
        return result;
    }

    public static Double[] toObjectArray(double arr[]){
        if(arr==null)
            return null;

        Double result[] = new Double[arr.length];
        for(int i=arr.length-1; i>=0; i--)
            result[i] = arr[i];
        return result;
    }

    /*-------------------------------------------------[ ensureCapacity ]---------------------------------------------------*/

    public static byte[] ensureCapacity(byte array[], int capacity){
        if(capacity<=0 || capacity-array.length<=0)
            return array;

        int newCapacity = array.length*2;
        if(newCapacity-capacity< 0)
            newCapacity = capacity;
        if(newCapacity<0){
            if(capacity<0) // overflow
                throw new OutOfMemoryError();
            newCapacity = Integer.MAX_VALUE;
        }
        return Arrays.copyOf(array, newCapacity);
    }

    public static char[] ensureCapacity(char array[], int capacity){
        if(capacity<=0 || capacity-array.length<=0)
            return array;

        int newCapacity = array.length*2;
        if(newCapacity-capacity< 0)
            newCapacity = capacity;
        if(newCapacity<0){
            if(capacity<0) // overflow
                throw new OutOfMemoryError();
            newCapacity = Integer.MAX_VALUE;
        }
        return Arrays.copyOf(array, newCapacity);
    }
}

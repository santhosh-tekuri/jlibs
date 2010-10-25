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

package jlibs.core.util;

import jlibs.core.graph.Filter;
import jlibs.core.lang.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class CollectionUtil{
    /**
     * Reads Properties from given inputStream and returns it.
     * NOTE: the given stream is closed by this method
     */
    public static Properties readProperties(InputStream is, Properties props) throws IOException{
        if(props==null)
            props = new Properties();
        try{
            props.load(is);
        }finally{
            is.close();
        }
        return props;
    }

    /**
     * Adds objects in array to the given collection
     *
     * @return the same collection which is passed as argument
     */
    @SuppressWarnings({"unchecked", "ManualArrayToCollectionCopy"})
    public static <E, T extends E> Collection<E> addAll(Collection<E> c, T... array){
        for(T obj: array)
            c.add(obj);
        return c;
    }

    /**
     * Removes objects in array to the given collection
     *
     * @return the same collection which is passed as argument
     */
    @SuppressWarnings("unchecked")
    public static <E, T extends E> Collection<E> removeAll(Collection<E> c, T... array){
        for(T obj: array)
            c.remove(obj);
        return c;
    }

    /**
     * Adds the given item to the list at specified <code>index</code>.
     * if <code>index</code> is greater than list size, it simply appends
     * to the list.
     */
    public static <E, T extends E> void add(List<E> list, int index, T item){
        if(index<list.size())
            list.add(index, item);
        else
            list.add(item);
    }

    /**
     * Returns List with elements from given collections which are selected
     * by specified filter
     */
    public static <T> List<T> filter(Collection<T> c, Filter<T> filter){
        if(c.size()==0)
            return Collections.emptyList();

        List<T> filteredList = new ArrayList<T>(c.size());
        for(T element: c){
            if(filter.select(element))
                filteredList.add(element);
        }
        return filteredList;
    }

    /**
     * returns key whose value matches with specified value from given map
     * if the given map contains multiple keys mapped to specified value, it
     * returns first key encountered
     */
    public static <K, V> K getKey(Map<K, V> map, V value){
        for(Map.Entry<K, V> entry : map.entrySet()){
            if(Util.equals(entry.getValue(), value))
                return entry.getKey();
        }
        return null;
    }

    /*-------------------------------------------------[ To Primitive Array ]---------------------------------------------------*/

    public static boolean[] toBooleanArray(Collection<Boolean> c){
        boolean arr[] = new boolean[c.size()];
        int i=0;
        for(Boolean item: c)
            arr[i++] = item;
        return arr;
    }

    public static int[] toIntArray(Collection<? extends Number> c){
        int arr[] = new int[c.size()];
        int i=0;
        for(Number item: c)
            arr[i++] = item.intValue();
        return arr;
    }

    public static long[] toLongArray(Collection<? extends Number> c){
        long arr[] = new long[c.size()];
        int i=0;
        for(Number item: c)
            arr[i++] = item.longValue();
        return arr;
    }

    public static float[] toFloatArray(Collection<? extends Number> c){
        float arr[] = new float[c.size()];
        int i=0;
        for(Number item: c)
            arr[i++] = item.floatValue();
        return arr;
    }

    public static double[] toDoubleArray(Collection<? extends Number> c){
        double arr[] = new double[c.size()];
        int i=0;
        for(Number item: c)
            arr[i++] = item.doubleValue();
        return arr;
    }

    public static byte[] toByteArray(Collection<? extends Number> c){
        byte arr[] = new byte[c.size()];
        int i=0;
        for(Number item: c)
            arr[i++] = item.byteValue();
        return arr;
    }

    public static short[] toShortArray(Collection<? extends Number> c){
        short arr[] = new short[c.size()];
        int i=0;
        for(Number item: c)
            arr[i++] = item.shortValue();
        return arr;
    }
}
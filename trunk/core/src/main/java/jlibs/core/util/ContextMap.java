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

import java.util.*;

/**
 * This class supports chaining of maps.
 * If the entry is not found in current map, it will
 * search in parent map
 *
 * @author Santhosh Kumar T
 */
public class ContextMap<K, V>{
    public ContextMap(){
        this(null);
    }

    public ContextMap(ContextMap<K, V> parent){
        setParent(parent);
    }

    private ContextMap<K, V> parent;
    public ContextMap<K, V> parent(){
        return parent;
    }

    public void setParent(ContextMap<K, V> parent){
        this.parent = parent;
    }

    private Map<K, V> map;
    public Map<K, V> map(){
        return map;
    }

    protected Map<K, V> createMap(){
        return new HashMap<K,V>();
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public V get(Object key){
        ContextMap<K, V> cmap = this;
        do{
            if(cmap.map!=null){
                V value = cmap.map.get(key);
                if(value!=null)
                    return value;
            }
            cmap = cmap.parent;
        }while(cmap!=null);
        return null;
    }

    public void put(K key, V value){
        if(map==null)
            map = createMap();
        map.put(key, value);
    }

    public void clear(){
        if(map!=null)
            map.clear();
    }

    public Iterator<K> keys(){
        Set<K> set = new HashSet<K>();
        ContextMap<K, V> cmap = this;
        do{
            if(cmap.map!=null)
                set.addAll(cmap.map.keySet());
            cmap = cmap.parent;
        }while(cmap!=null);
        return set.iterator();
    }
}

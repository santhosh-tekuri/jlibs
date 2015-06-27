/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

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

import java.util.Comparator;

/**
 * @author Santhosh Kumar T
 */
public class ReverseComparator<T> implements Comparator<T>{
    private Comparator<T> delegate;

    public ReverseComparator(){
        this(new DefaultComparator<T>());
    }
    
    public ReverseComparator(Comparator<T> delegate){
        this.delegate = delegate;
    }

    public int compare(T o1, T o2){
        return -delegate.compare(o1, o2);
    }
}

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

package jlibs.core.graph.sequences;

/**
 * @author Santhosh Kumar T
 */
public class Element<E>{
    private int index;
    private E item;

    public Element(){
        this(-1, null);
    }
    
    public Element(int index, E item){
        set(index, item);
    }

    public void set(int index, E item){
        this.index = index;
        this.item = item;
    }

    public void set(E item){
        this.item = item;
        index++;
    }

    public E get(){
        return item;
    }

    public int index(){
        return index;
    }

    public boolean finished(){
        return index>=0 && item==null;
    }
    
    public void reset(){
        index = -1;
        item = null;
    }
}


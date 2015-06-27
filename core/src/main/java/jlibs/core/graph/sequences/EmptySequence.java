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

package jlibs.core.graph.sequences;

import jlibs.core.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public final class EmptySequence<E> implements Sequence<E>{
    private static final EmptySequence INSTANCE = new EmptySequence();

    @SuppressWarnings("unchecked")
    public static <T> EmptySequence<T> getInstance(){
        return (EmptySequence<T>)INSTANCE;
    }

    private EmptySequence(){}

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    @Override
    public boolean hasNext(){
        return false;
    }

    @Override
    public E next(){
        return null;
    }

    @Override
    public E next(int count){
        return null;
    }

    /*-------------------------------------------------[ Query ]---------------------------------------------------*/

    @Override
    public int index(){
        return 0;
    }

    @Override
    public E current(){
        return null;
    }

    @Override
    public int length(){
        return 0;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){}

    @Override
    public EmptySequence<E> copy(){
        return this;
    }
}

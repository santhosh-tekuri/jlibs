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
public abstract class AbstractSequence<E> implements Sequence<E>{
    protected AbstractSequence(){
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/
    
    private boolean advanced = false;
    private E next;

    @Override
    public boolean hasNext(){
        if(current.finished())
            return false;
        else if(!advanced){
            next = findNext();
            advanced = true;
        }
        return next!=null;
    }

    @Override
    public final E next(){
        if(current.finished())
            return null;
        else if(advanced){
            advanced = false;
            current.set(next);
            next = null;
        }else
            current.set(findNext());

        return current.get();
    }

    @Override
    public final E next(int count){
        if(current.finished())
            return null;
        
        if(count<=0)
            return current();
        else if(count==1)
            return next();

        if(advanced){
            next();
            return next(count-1);
        }

        Element<E> elem = findNext(count);
        if(elem==null){
            while(count>0){
                if(next()==null)
                    break;
                count--;
            }
        }else
            current = elem;
        
        return current();
    }

    protected abstract E findNext();
    @SuppressWarnings({"UnusedDeclaration"})
    protected Element<E> findNext(int count){
        return null;
    }

    /*-------------------------------------------------[ Query ]---------------------------------------------------*/
    
    protected Element<E> current = new Element<E>();

    @Override
    public int index(){
        return current.index();
    }

    @Override
    public final E current(){
        return current.get();
    }

    @Override
    public int length(){
        Sequence<E> seq = copy();

        int len = 0;
        while(seq.next()!=null)
            len++;

        return len;
    }

    /*-------------------------------------------------[ Reset ]---------------------------------------------------*/

    private void _reset(){
        current.reset();
        next = null;
        advanced = false;
    }

    public void reset(){
        _reset();
    }    
}

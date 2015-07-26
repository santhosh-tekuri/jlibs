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

package jlibs.core.graph;

/**
 * @author Santhosh Kumar T
 */
public final class Path{
    private Path parentPath;
    private Object elem;
    private int index;
    public boolean lastElem = false;

    public Path(Object elem){
        if(elem==null)
            throw new IllegalArgumentException("element in path must be non null");
        this.elem = elem;
        this.index = 0;
    }

    private Path(Path parentPath, Object elem, int index){
        this.parentPath = parentPath;
        this.elem = elem;
        this.index = index;
    }

    public Path append(Object elem){
        return append(elem, -1);
    }

    public Path append(Object elem, int index){
        if(elem==null)
            throw new IllegalArgumentException("element in path must be non null");
        return new Path(this, elem, index);
    }

    public Path getParentPath(){
        return parentPath;
    }

    public Path getParentPath(Class clazz){
        Path path = this;
        do{
            path = path.parentPath;
        }while(path!=null && !clazz.isInstance(path.elem));
        return path;
    }

    public Object getElement(){
        return elem;
    }

    public int getIndex(){
        return index;
    }

    public Object getElement(int i){
        if(i<0)
            throw new IndexOutOfBoundsException("negative index: "+i);
        int len = getLength();
        if(i>=len)
            throw new IndexOutOfBoundsException(String.format("index %d is out of range", i));
        len--;
        Path path = this;
        while(len!=i)
            path = path.parentPath;
        return path.elem;
    }

    public int getLength(){
        int len = 0;
        for(Path path=this; path!=null; path=path.parentPath)
            len++;
        return len;
    }

    public Object[] toArray(){
        Object array[] = new Object[getLength()];
        Path path = this;
        for(int i=getLength(); i>0; i--){
            array[i-1] = path.elem;
            path = path.parentPath;
        }
        return array;
    }

    public int getRecursionDepth(){
        int depth = -1;
        Path path = this;
        for(int i=getLength(); i>0; i--){
            if(path.elem==elem)
                depth++;
            path = path.parentPath;
        }
        return depth;
    }

    public String toString(){
        StringBuilder buff = new StringBuilder();
        for(Path path=this; path!=null; path=path.parentPath){
            if(buff.length()>0)
                buff.insert(0, ", ");
            buff.insert(0, path.elem.toString());
        }
        return buff.toString();
    }
}

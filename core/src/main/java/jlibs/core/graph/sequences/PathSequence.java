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

import jlibs.core.graph.Path;
import jlibs.core.graph.Sequence;

/**
 * @author Santhosh Kumar T
 */
public class PathSequence<E> extends AbstractSequence<Path>{
    private Path path;
    private Sequence<? extends E> delegate;

    public PathSequence(Path path, Sequence<? extends E> delegate){
        this.path = path;
        this.delegate = delegate;
        _reset();
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    @Override
    protected Path findNext(){
        E elem = delegate.next();
        return elem==null ? null : path.append(elem, index()+1);
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        delegate.reset();
    }

    @Override
    public Sequence<Path> copy(){
        return new PathSequence<E>(path, delegate.copy());
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return delegate.length();
    }
}

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

package jlibs.core.graph.navigators;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.sequences.FilteredTreeSequence;
import jlibs.core.graph.Filter;
import jlibs.core.graph.Navigator;

/**
 * @author Santhosh Kumar T
 */
public class FilteredTreeNavigator<E> implements Navigator<E>{
    private Navigator<E> delegate;
    private Filter<E> filter;

    public FilteredTreeNavigator(Navigator<E> delegate){
        this(delegate, null);
    }

    public FilteredTreeNavigator(Navigator<E> delegate, Filter<E> filter){
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public Sequence<? extends E> children(E elem){
        Sequence<? extends E> seq = delegate.children(elem);
        if(seq==null)
            return EmptySequence.getInstance();
        if(filter!=null)
            seq = new FilteredTreeSequence<E>(seq, this, filter);
        return seq;
    }
}
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

package jlibs.swing.tree;

import jlibs.core.graph.Navigator;
import jlibs.core.graph.SequenceUtil;

/**
 * @author Santhosh Kumar T
 */
public class NavigatorTreeModel extends AbstractTreeModel{
    private Object root;
    private Navigator<Object> navigator;

    @SuppressWarnings({"unchecked"})
    public <E> NavigatorTreeModel(E root, Navigator<E> navigator){
        this.root = root;
        this.navigator = (Navigator<Object>)navigator;
    }

    @Override
    public Object getRoot(){
        return root;
    }

    @Override
    public Object getChild(Object parent, int index){
        return navigator.children(parent).next(index+1);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child){
        return SequenceUtil.indexOf(navigator.children(parent), child);
    }

    @Override
    public int getChildCount(Object parent){
        return navigator.children(parent).length();
    }

    @Override
    public boolean isLeaf(Object node){
        // node can be null : javax.swing.JTable$AccessibleJTable.getAccessibleAt(JTable.java:6982)
        return node==null || navigator.children(node).length()==0;
    }
}

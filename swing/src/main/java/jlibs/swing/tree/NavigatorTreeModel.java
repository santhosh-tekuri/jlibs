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
        return navigator.children(node).length()==0;
    }
}

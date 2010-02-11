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

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author Santhosh Kumar T
 */
public abstract class AbstractTreeModel implements TreeModel{
    protected Object root;

    protected AbstractTreeModel(){
    }

    protected AbstractTreeModel(Object root){
        this.root = root;
    }

    public void setRoot(Object root){
        this.root = root;
        fireTreeStructureChanged(root, new Object[]{ root }, null, null);
    }

    /*-------------------------------------------------[ Listeners ]---------------------------------------------------*/

    protected EventListenerList listenerList = new EventListenerList();

    @Override
    public void addTreeModelListener(TreeModelListener listener){
        listenerList.add(TreeModelListener.class, listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener){
        listenerList.remove(TreeModelListener.class, listener);
    }

    /*-------------------------------------------------[ Firing Changes ]---------------------------------------------------*/
    
    @Override
    public void valueForPathChanged(TreePath path, Object newValue){
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for(int i = listeners.length-2; i>=0; i-=2){
            if(e==null)
                e = new TreeModelEvent(this, path.getPath(), null, null);
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
        }
    }

    public void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children){
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for(int i = listeners.length-2; i>=0; i-=2){
            if(e==null)
                e = new TreeModelEvent(source, path, childIndices, children);
            ((TreeModelListener)listeners[i+1]).treeStructureChanged(e);
        }
    }

    public void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children){
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for(int i = listeners.length-2; i>=0; i-=2){
            if(e==null)
                e = new TreeModelEvent(source, path, childIndices, children);
            ((TreeModelListener)listeners[i+1]).treeNodesInserted(e);
        }
    }

    public void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children){
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        for(int i = listeners.length-2; i>=0; i-=2){
            if(e==null)
                e = new TreeModelEvent(source, path, childIndices, children);
            ((TreeModelListener)listeners[i+1]).treeNodesRemoved(e);
        }
    }
}

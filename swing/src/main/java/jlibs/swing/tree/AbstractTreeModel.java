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

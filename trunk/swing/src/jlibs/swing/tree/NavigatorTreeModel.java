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

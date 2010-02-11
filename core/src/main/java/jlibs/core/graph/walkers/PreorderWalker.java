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

package jlibs.core.graph.walkers;

import jlibs.core.graph.*;
import jlibs.core.graph.sequences.AbstractSequence;
import jlibs.core.graph.sequences.DuplicateSequence;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.sequences.EnumeratedSequence;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Stack;

/**
 * @author Santhosh Kumar T
 */
public class PreorderWalker<E> extends AbstractSequence<E> implements Walker<E>{
    private Sequence<? extends E> seq;
    private Navigator<E> navigator;

    public PreorderWalker(Sequence<? extends E> seq, Navigator<E> navigator){
        this.seq = seq;
        this.navigator = navigator;
        _reset();
    }

    public PreorderWalker(E elem, Navigator<E> navigator){
        this(new DuplicateSequence<E>(elem), navigator);
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        path = null;
        stack.clear();
        stack.push(new Children(seq.copy()));
    }

    @Override
    public PreorderWalker<E> copy(){
        return new PreorderWalker<E>(seq.copy(), navigator);
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/
    
    private Stack<Children> stack = new Stack<Children>();
    private Path path;

    private class Children{
        boolean breakpoint;
        Sequence<? extends E> seq;

        public Children(Sequence<? extends E> seq){
            this.seq = seq;
        }
    }

    @Override
    protected E findNext(){
        // pop empty sequences
        while(!stack.isEmpty()){
            Children elem = stack.peek();
            if(elem.seq.next()==null){
                if(elem.breakpoint)
                    return null;
                else{
                    stack.pop();
                    if(path!=null)
                        path = path.getParentPath();
                }
            }else
                break;
        }
        
        if(stack.isEmpty())
            return null;
        else{
            Sequence<? extends E> peekSeq = stack.peek().seq;
            E current = peekSeq.current();
            path = path==null ? new Path(current) : path.append(current, peekSeq.index());
            path.lastElem = !peekSeq.hasNext();
            stack.push(new Children(navigator.children(current)));
            return current;
        }
    }

    /*-------------------------------------------------[ Walker ]---------------------------------------------------*/

    public Path getCurrentPath(){
        return path;
    }

    public void skip(){
        if(stack.isEmpty())
            throw new IllegalStateException("can't skip of descendants of null");
        stack.peek().seq = EmptySequence.getInstance();
    }
    
    public void addBreakpoint(){
        if(stack.isEmpty())
            throw new IllegalStateException("can't add breakpoint on empty sequence");
        stack.peek().breakpoint = true;
    }

    public boolean isPaused(){
        return !stack.isEmpty() && stack.peek().breakpoint;
    }

    @SuppressWarnings("unchecked")
    public void resume(){
        if(isPaused()){
            stack.peek().breakpoint = false;
            current.set(current.index()-1, (E)path.getElement());
        }
    }

    public static void main(String[] args){
        Class<Number> c1 = Number.class;
        Class<Integer> c2 = Integer.class;
        System.out.println(c1.isAssignableFrom(c2));
        System.out.println(c1.isAssignableFrom(c2));

        JFrame frame = new JFrame();
        JTree tree = new JTree();
        frame.getContentPane().add(new JScrollPane(tree));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        final PreorderWalker<DefaultMutableTreeNode> seq = new PreorderWalker<DefaultMutableTreeNode>((DefaultMutableTreeNode)tree.getModel().getRoot(), new Navigator<DefaultMutableTreeNode>(){
            @Override
            @SuppressWarnings({"unchecked"})
            public Sequence<DefaultMutableTreeNode> children(DefaultMutableTreeNode elem){
                return new EnumeratedSequence<DefaultMutableTreeNode>(elem.children());
            }
        });
        WalkerUtil.print(seq, null);
    }
}

package jlibs.core.graph.walkers;

import jlibs.core.graph.sequences.*;
import jlibs.core.graph.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Stack;

/**
 * @author Santhosh Kumar T
 */
public class PreorderWalker<E> extends AbstractSequence<E> implements Walker<E>{
    private E elem;
    private Navigator<E> navigator;

    public PreorderWalker(E elem, Navigator<E> navigator){
        this.elem = elem;
        this.navigator = navigator;
        _reset();
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
        stack.push(new Children(new DuplicateSequence<E>(elem)));
    }

    @Override
    public PreorderWalker<E> copy(){
        return new PreorderWalker<E>(elem, navigator);
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
            E current = stack.peek().seq.current();
            stack.push(new Children(navigator.children(current)));
            path = path==null ? new Path(current) : path.append(current);
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
        Class c1 = Number.class;
        Class c2 = Integer.class;
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
            public Sequence<DefaultMutableTreeNode> children(DefaultMutableTreeNode elem){
                return new EnumeratedSequence<DefaultMutableTreeNode>(elem.children());
            }
        });

        WalkerUtil.walk(seq, new Processor<DefaultMutableTreeNode>(){
            int indent = 0;

            @Override
            public boolean preProcess(DefaultMutableTreeNode elem, Path path){
                for(int i=0; i<indent; i++)
                    System.out.print("   ");
                System.out.println(elem+" "+seq.index());
                indent++;
                return true;
            }

            @Override
            public void postProcess(DefaultMutableTreeNode elem, Path path){
                indent--;
//                for(int i=0; i<indent; i++)
//                    System.out.print("   ");
//                System.out.println("</"+elem+">"+path);
            }
        });
    }
}

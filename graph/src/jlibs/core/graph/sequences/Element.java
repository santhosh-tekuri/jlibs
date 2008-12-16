package jlibs.core.graph.sequences;

/**
 * @author Santhosh Kumar T
 */
public class Element<E>{
    private int index;
    private E item;

    public Element(){
        this(-1, null);
    }
    
    public Element(int index, E item){
        set(index, item);
    }

    public void set(int index, E item){
        this.index = index;
        this.item = item;
    }

    public void set(E item){
        this.item = item;
        index++;
    }

    public E get(){
        return item;
    }

    public int index(){
        return index;
    }

    public boolean finished(){
        return index>=0 && item==null;
    }
    
    public void reset(){
        index = -1;
        item = null;
    }
}


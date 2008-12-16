package jlibs.core.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Walker<E> extends Sequence<E>{
    public Path getCurrentPath();
    public void skip();
    public void addBreakpoint();
    public boolean isPaused();
    public void resume();
}

package jlibs.core.graph;

/**
 * @author Santhosh Kumar T
 */
public interface Processor<E>{
    public boolean preProcess(E elem, Path path);
    public void postProcess(E elem, Path path);
}

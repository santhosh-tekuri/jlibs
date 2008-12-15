package jlibs.graph.filters;

import jlibs.graph.Filter;
import jlibs.graph.Path;

/**
 * @author Santhosh Kumar T
 */
public class PathFilter<E> implements Filter<E>{
    private Path path;
    private Filter<Path> delegate;

    public PathFilter(Path path, Filter<Path> delegate){
        this.path = path;
        this.delegate = delegate;
    }

    @Override
    public boolean select(E elem){
        return delegate.select(path.append(elem));
    }
}

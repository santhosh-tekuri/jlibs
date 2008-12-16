package jlibs.core.graph.navigators;

import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.PathSequence;
import jlibs.core.graph.Navigator;
import jlibs.core.graph.Path;

/**
 * @author Santhosh Kumar T
 */
public class PathNavigator<E> implements Navigator<Path>{
    private Navigator<E> delegate;

    public PathNavigator(Navigator<E> delegate){
        this.delegate = delegate;
    }

    @Override
    public Sequence<Path> children(Path path){
        @SuppressWarnings({"unchecked"})
        Sequence<? extends E> seq = delegate.children((E)path.getElement());
        return new PathSequence<E>(path, seq);
    }
}

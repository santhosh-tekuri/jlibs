package jlibs.graph.navigators;

import jlibs.graph.Navigator;
import jlibs.graph.Path;
import jlibs.graph.Sequence;
import jlibs.graph.sequences.PathSequence;

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

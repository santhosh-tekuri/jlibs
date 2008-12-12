package jlibs.graph;

import jlibs.graph.navigators.FilteredNavigator;
import jlibs.graph.sequences.ArraySequence;
import jlibs.graph.sequences.EmptySequence;
import jlibs.graph.walkers.PreorderWalker;
import jlibs.graph.visitors.StaticVisitor;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class WalkerUtil{
    public static <E> void walk(Walker<E> walker, Visitor<E, Processor<E>> visitor){
        while(true){
            E elem = walker.next();
            if(elem!=null){
                Processor<E> processor = visitor.visit(elem);
                if(processor!=null){
                    if(!processor.preProcess(elem, walker.getCurrentPath()))
                        walker.skip();
                }
                walker.addBreakpoint();
            }else if(walker.isPaused()){
                walker.resume();
                elem = walker.current();
                Processor<E> processor = visitor.visit(elem);
                if(processor!=null)
                    processor.postProcess(elem, walker.getCurrentPath());
            }else
                return;
        }
    }

    public static <E> void walk(Walker<E> walker, final Processor<E> processor){
        walk(walker, new StaticVisitor<E, Processor<E>>(processor));
    }

    public static <E> List<E> topologicalSort(Sequence<E> elements, final Navigator<E> navigator){
        final List<E> unvisitedElements = SequenceUtil.addAll(new LinkedList<E>(), elements);
        Navigator<E> filteredNavigator = new FilteredNavigator<E>(navigator, new Filter<E>(){
            @Override
            public boolean select(E elem){
                return unvisitedElements.contains(elem);
            }
        });

        final LinkedList<E> result = new LinkedList<E>();
        while(!unvisitedElements.isEmpty()){
            WalkerUtil.walk(new PreorderWalker<E>(unvisitedElements.remove(0), filteredNavigator), new Processor<E>(){
                @Override
                public boolean preProcess(E elem, Path path){
                    unvisitedElements.remove(elem);
                    return true;
                }

                @Override
                public void postProcess(E elem, Path path){
                    result.add(0, elem);
                }
            });
        }
        return result;
    }

    public static void main(String[] args){
        ArraySequence<String> elements = new ArraySequence<String>("undershorts", "socks", "pants", "shoes", "watch", "belt", "shirt", "tie", "jacket");
        System.out.println(topologicalSort(elements, new Navigator<String>(){
            Map<String, Sequence<String>> map = new HashMap<String, Sequence<String>>();
            {
                map.put("undershorts", new ArraySequence<String>("pants", "shoes"));
                map.put("socks", new ArraySequence<String>("shoes"));
                map.put("pants", new ArraySequence<String>("belt", "shoes"));
                map.put("belt", new ArraySequence<String>("jacket"));
                map.put("shirt", new ArraySequence<String>("tie"));
                map.put("tie", new ArraySequence<String>("jacket"));
            }
            
            @Override
            public Sequence<String> children(String elem){
                Sequence<String> seq = map.get(elem);
                if(seq==null)
                    return EmptySequence.getInstance();
                else
                    return seq;
            }
        }));
    }
}

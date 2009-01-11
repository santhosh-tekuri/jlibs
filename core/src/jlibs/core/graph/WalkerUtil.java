/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.graph;

import jlibs.core.graph.navigators.FilteredNavigator;
import jlibs.core.graph.sequences.ArraySequence;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.visitors.StaticVisitor;
import jlibs.core.graph.walkers.PreorderWalker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public static <E> List<E> topologicalSort(Sequence<E> elements, Navigator<E> navigator){
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

        for(int i=0; i<result.size(); i++){
            Sequence<? extends E> seq = navigator.children(result.get(i));
            for(E elem; (elem=seq.next())!=null;){
                if(result.indexOf(elem)<i)
                    throw new IllegalArgumentException("the given graph contains cycle");
            }
        }
        return result;
    }

    public static <E> void print(Walker<E> walker, final Visitor<E, String> visitor){
        walk(walker, new Processor<E>(){
            int indent = 0;

            @Override
            public boolean preProcess(E elem, Path path){
                for(int i=0; i<indent; i++)
                    System.out.print("   ");
                System.out.println(visitor!=null ? visitor.visit(elem) : elem.toString());
                indent++;
                return true;
            }

            @Override
            public void postProcess(E elem, Path path){
                indent--;
            }
        });
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

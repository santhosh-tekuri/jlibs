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

package jlibs.core.graph;

import jlibs.core.graph.navigators.FilteredNavigator;
import jlibs.core.graph.visitors.StaticVisitor;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.core.io.FileNavigator;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

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
            private StringBuilder getIndentation(Path path){
                Deque<Path> stack = new ArrayDeque<Path>();
                while(path.getParentPath()!=null){
                    stack.push(path);
                    path = path.getParentPath();
                }
                StringBuilder indentString = new StringBuilder();
                while(!stack.isEmpty()){
                    path = stack.pop();
                    if(stack.isEmpty()){
                        indentString.append(path.lastElem ? '`' : '|');
                        indentString.append("-- ");
                    }else{
                        indentString.append(path.lastElem ? ' ' : '|');
                        indentString.append("   ");
                    }
                }
                return indentString;
            }

            @Override
            public boolean preProcess(E elem, Path path){
                printPending(path);
                String str = visitor!=null ? visitor.visit(elem) : elem.toString();
                System.out.print(getIndentation(path));

                int newLine = str.indexOf('\n');
                if(newLine!=-1){
                    pending = str.substring(newLine+1);
                    pendingPath = path;
                    str = str.substring(0, newLine);
                }

                System.out.println(str);

                return true;
            }

            String pending;
            Path pendingPath;
            private void printPending(Path path){
                if(pending!=null){
                    StringBuilder indentStr = getIndentation(path.getParentPath()==null ? pendingPath : path);
                    if(indentStr.length()>0){
                        indentStr.replace(indentStr.length()-4, indentStr.length(), path.getParentPath()==null ? "   " : "|");
                        if(path.getParentPath()==pendingPath.getParentPath())
                            indentStr.append("   ");
                    }

                    int from = 0;
                    int index = 0;
                    while((index=pending.indexOf('\n', from))!=-1){
                        System.out.print(indentStr);
                        System.out.println(pending.substring(from, index));
                        from = index+1;
                    }

                    if(from<pending.length()){
                        System.out.print(indentStr);
                        System.out.println(pending.substring(from));
                    }
                    pending = null;
                }
            }
            
            @Override
            public void postProcess(E elem, Path path){
                if(path.getParentPath()==null)
                    printPending(path);
            }
        });
    }

    public static void main(String[] args){
//        ArraySequence<String> elements = new ArraySequence<String>("undershorts", "socks", "pants", "shoes", "watch", "belt", "shirt", "tie", "jacket");
//        System.out.println(topologicalSort(elements, new Navigator<String>(){
//            Map<String, Sequence<String>> map = new HashMap<String, Sequence<String>>();
//            {
//                map.put("undershorts", new ArraySequence<String>("pants", "shoes"));
//                map.put("socks", new ArraySequence<String>("shoes"));
//                map.put("pants", new ArraySequence<String>("belt", "shoes"));
//                map.put("belt", new ArraySequence<String>("jacket"));
//                map.put("shirt", new ArraySequence<String>("tie"));
//                map.put("tie", new ArraySequence<String>("jacket"));
//            }
//
//            @Override
//            public Sequence<String> children(String elem){
//                Sequence<String> seq = map.get(elem);
//                if(seq==null)
//                    return EmptySequence.getInstance();
//                else
//                    return seq;
//            }
//        }));
        print(new PreorderWalker<File>(new File("/Volumes/Softwares/Personal/jlibs/core/src"), new FileNavigator(new FileFilter(){
            @Override
            public boolean accept(File file){
                return !file.isDirectory() || !file.getName().equals(".svn");
            }
        })), new Visitor<File, String>(){
            @Override
            public String visit(File elem){
                return elem.getName();
            }
        });
    }
}

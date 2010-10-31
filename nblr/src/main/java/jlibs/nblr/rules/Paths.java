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

package jlibs.nblr.rules;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class Paths extends ArrayList<Path>{
    public final Path owner;
    public final int depth;

    public Paths(Path owner){
        this.owner = owner;
        if(owner!=null){
            owner.children = this;
            depth = owner.depth+1;
        }else
            depth = 1;
    }

    public boolean add(Path path){
        if(owner==null)
            path.branch = size();
        else
            path.branch = owner.branch;

        path.parent = owner;
        path.depth = depth;
        return super.add(path);
    }

    public List<Path> leafs(){
        List<Path> list = new ArrayList<Path>();
        leafs(list);
        return list;
    }
    
    private void leafs(List<Path> list){
        for(Path path: this){
            if(path.children==null)
                list.add(path);
            else
                path.children.leafs(list);
        }
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    private static boolean clashes(Path p1, Path p2){
        if(p1.matcher()==null && p2.matcher()==null)
            throw new IllegalStateException("Ambiguous Routes: "+p1+" AND "+p2);
        if(p1.matcher()!=null && p2.matcher()!=null){
            if(p1.fallback() || p2.fallback())
                return false;
            else
                return p1.clashesWith(p2);
        }
        return false;
    }

    public static Paths travel(Node fromNode, boolean digIntoRule){
        Paths rootPaths = new Paths(null);
        List<Path> list = new ArrayList<Path>();

        while(true){
            if(list.size()==0){
                rootPaths.populate(fromNode, digIntoRule);
                list.addAll(rootPaths);
            }else{
                List<Path> newList = new ArrayList<Path>();
                for(Path path: list){
                    if(path.matcher()!=null){
                        Paths paths = new Paths(path);
                        paths.populate((Node)path.get(path.size()-1), true);
                        newList.addAll(paths);
                    }
                }
                list = newList;
            }

            TreeSet<Integer> clashingIndexes = new TreeSet<Integer>();
            for(int ibranch=0; ibranch<rootPaths.size()-1; ibranch++){
                for(int jbranch=ibranch+1; jbranch<rootPaths.size(); jbranch++){
                    int i = 0;
                    for(Path ipath: list){
                        if(ipath.branch==ibranch){
                            int j = 0;
                            for(Path jpath: list){
                                if(jpath.branch==jbranch){
                                    if(clashes(ipath, jpath)){
                                        if(ipath.hasLoop() && jpath.hasLoop())
                                            throw new IllegalStateException("Infinite lookAhead needed: "+ipath+" and "+jpath);
                                        clashingIndexes.add(i);
                                        clashingIndexes.add(j);
                                    }
                                }
                                j++;
                            }
                        }
                        i++;
                    }
                }
            }

            if(clashingIndexes.size()==0)
                return rootPaths;

            List<Path> clashingPaths = new ArrayList<Path>(clashingIndexes.size());
            for(int id: clashingIndexes)
                clashingPaths.add(list.get(id));
            list = clashingPaths;
        }
    }

    private void populate(Node fromNode, boolean digIntoRule){
        populate(fromNode, new ArrayDeque<Object>(), digIntoRule);
    }

    private void populate(Node fromNode, Deque<Object> stack, boolean digIntoRule){
        if(stack.contains(fromNode))
            throw new IllegalStateException("infinite loop detected");

        stack.push(fromNode);
        if(fromNode.outgoing.size()>0){
            if(fromNode.outgoing.size()>1)
                digIntoRule = true;
            for(Edge edge: fromNode.outgoing){
                stack.push(edge);
                if(edge.matcher!=null){
                    stack.push(edge.target);
                    add(new Path(stack));
                    stack.pop();
                }else if(edge.ruleTarget!=null){
                    if(!digIntoRule){
                        if(new Routes(edge.ruleTarget.rule, edge.ruleTarget.node(), true).routeStartingWithEOF!=null) // the rule can match nothing
                            digIntoRule = true;
                    }
                    if(!digIntoRule){
                        stack.push(edge.ruleTarget.node());
                        add(new Path(stack));
                        stack.pop();
                    } else{
                        populate(edge.ruleTarget.node(), stack, digIntoRule);
                    }
                }else
                    populate(edge.target, stack, digIntoRule);
                stack.pop();
            }
        }else{
            Path temp = new Path(stack);
            temp.parent = this.owner;
            Node target = temp.nodeAfterPop();
            if(target==null)
                add(new Path(stack));
            else
                populate(target, stack, digIntoRule);
        }
        stack.pop();
    }
}

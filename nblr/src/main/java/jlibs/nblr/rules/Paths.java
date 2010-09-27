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

    @SuppressWarnings({"SimplifiableIfStatement"})
    private static boolean clashes(Path p1, Path p2){
        if(p1.matcher()==null && p2.matcher()==null)
            throw new IllegalStateException("Ambigous Routes");
        if(p1.matcher()!=null && p2.matcher()!=null){
            if(p1.matcherEdge().fallback || p2.matcherEdge().fallback)
                return false;
            else
                return p1.clashesWith(p2);
        }
        return false;
    }

    public static Paths travel(Node fromNode){
        Paths rootPaths = new Paths(null);
        List<Path> list = new ArrayList<Path>();

        while(true){
            if(list.size()==0){
                rootPaths.populate(fromNode, new ArrayDeque<Object>());
                list.addAll(rootPaths);
            }else{
                List<Path> newList = new ArrayList<Path>();
                for(Path path: list){
                    if(path.matcher()!=null){
                        Paths paths = new Paths(path);
                        paths.populate((Node)path.get(path.size()-1));
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
                                            throw new IllegalStateException("Infinite lookAhead needed");
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

    private void populate(Node fromNode){
        populate(fromNode, new ArrayDeque<Object>());
    }

    private void populate(Node fromNode, Deque<Object> stack){
        if(stack.contains(fromNode))
            throw new IllegalStateException("infinite loop detected");

        stack.push(fromNode);
        if(fromNode.outgoing.size()>0){
            for(Edge edge: fromNode.outgoing){
                stack.push(edge);
                if(edge.matcher!=null){
                    stack.push(edge.target);
                    add(new Path(stack));
                    stack.pop();
                }else if(edge.rule!=null)
                    populate(edge.rule.node, stack);
                else
                    populate(edge.target, stack);
                stack.pop();
            }
        }else{
            int rulesPopped = 0;
            boolean wasNode = false;
            Node target = null;
            for(Object obj: stack){
                if(obj instanceof Node){
                    if(wasNode)
                        rulesPopped++;
                    wasNode = true;
                }else if(obj instanceof Edge){
                    wasNode = false;
                    Edge edge = (Edge)obj;
                    if(edge.rule!=null){
                        if(rulesPopped==0){
                            target = edge.target;
                            break;
                        }else
                            rulesPopped--;
                    }
                }
            }

            Path p = this.owner;
            while(p!=null && target==null){
                wasNode = false;
                for(int i=p.size()-1; i>=0; i--){
                    Object obj = p.get(i);
                    if(obj instanceof Node){
                        if(wasNode)
                            rulesPopped++;
                        wasNode = true;
                    }else if(obj instanceof Edge){
                        wasNode = false;
                        Edge edge = (Edge)obj;
                        if(edge.rule!=null){
                            if(rulesPopped==0){
                                target = edge.target;
                                break;
                            }else
                                rulesPopped--;
                        }
                    }
                }
                p = p.parent;
            }

            if(target==null){
                add(new Path(stack));
            }else
                populate(target, stack);
        }
        stack.pop();
    }
}

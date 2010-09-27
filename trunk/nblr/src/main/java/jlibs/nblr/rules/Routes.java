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

import jlibs.core.lang.ImpossibleException;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class Routes{
    public final Node fromNode;
    public final Paths paths;
    public final int maxLookAhead;
    public final List<Path[]> determinateBranchRoutes;
    public final List<Path[]> indeterminateBranchRoutes;
    public final Path[] routeStartingWithEOF;

    @SuppressWarnings({"unchecked"})
    public Routes(Node fromNode){
        this.fromNode = fromNode;
        this.paths = Paths.travel(fromNode);

        List<Path[]> routes = flatten();

        // split paths into branches and find maxLookAhead required
        int maxLookAhead = 0;
        List<Path[]> branches[] = new List[paths.size()];
        for(Path[] route: routes){
            int branch = route[0].branch;
            if(branches[branch]==null)
                branches[branch] = new ArrayList<Path[]>();
            branches[branch].add(route);
            maxLookAhead = Math.max(maxLookAhead, route.length);
        }
        this.maxLookAhead = maxLookAhead;

        // find branch with multiple paths
        int branchWithMultiplePaths = -1;
        for(int branch=0; branch<branches.length; branch++){
            if(branches[branch].size()>1){
                if(branchWithMultiplePaths==-1)
                    branchWithMultiplePaths = branch;
                else
                    throw new ImpossibleException("branches "+branch+" and "+branchWithMultiplePaths+" have multiple paths");
            }
        }

        if(branchWithMultiplePaths==-1)
             indeterminateBranchRoutes = new ArrayList<Path[]>();
        else
            indeterminateBranchRoutes = branches[branchWithMultiplePaths];

        determinateBranchRoutes = new ArrayList<Path[]>();
        for(int branch=0; branch<branches.length; branch++){
            if(branch!=branchWithMultiplePaths)
                determinateBranchRoutes.addAll(branches[branch]);
        }
        Collections.sort(determinateBranchRoutes, new Comparator<Path[]>(){
            @Override
            public int compare(Path[] route1, Path[] route2){
                int diff = route1.length - route2.length;
                if(diff==0){
                    if(route1[route1.length-1].fallback())
                        return +1;
                    else if(route2[route2.length-1].fallback())
                        return -1;
                    else
                        return 0;
                }else
                    return diff;
            }
        });

        Path[] routeStartingWithEOF = null;
        for(Path[] route: determinateBranchRoutes){
            if(route[0].matcher()==null){
                if(routeStartingWithEOF!=null)
                    throw new ImpossibleException("found more that one route starting with <EOF>");
                routeStartingWithEOF = route;
            }
        }
        if(routeStartingWithEOF!=null)
            determinateBranchRoutes.remove(routeStartingWithEOF);
        this.routeStartingWithEOF = routeStartingWithEOF;
    }

    private List<Path[]> flatten(){
        List<Path[]> routes = new ArrayList<Path[]>();
        flatten(paths, new ArrayDeque<Path>(), routes);
        return routes;
    }

    private void flatten(Paths paths, Deque<Path> pathStack, List<Path[]> routes){
        for(Path path: paths)
            flatten(path, pathStack, routes);
    }

    private void flatten(Path path, Deque<Path> pathStack, List<Path[]> routes){
        pathStack.push(path);
        if(path.children==null){
            List<Path> route = new ArrayList<Path>(pathStack);
            Collections.reverse(route);
            routes.add(route.toArray(new Path[route.size()]));
        }else
            flatten(path.children, pathStack, routes);
        pathStack.pop();
    }

    public String toString(){
        StringBuilder buff = new StringBuilder();
        for(Path[] route: determinateBranchRoutes)
            add(buff, route);
        for(Path[] route: indeterminateBranchRoutes)
            add(buff, route);
        if(routeStartingWithEOF!=null)
            add(buff, routeStartingWithEOF);

        return buff.toString();
    }

    private void add(StringBuilder buff, Path[] route){
        if(buff.length()>0)
            buff.append(" OR ");
        for(Path path: route)
            buff.append(path);
    }
}

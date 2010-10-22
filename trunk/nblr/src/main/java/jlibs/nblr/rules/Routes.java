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
    public final Rule rule;
    public final Node fromNode;
    public final Paths paths;
    public final int maxLookAhead;
    public final List<Path> determinateRoutes;
    public final Path indeterminateRoute;
    public final Path routeStartingWithEOF;

    @SuppressWarnings({"unchecked"})
    public Routes(Rule rule, Node fromNode){
        this.rule = rule;
        this.fromNode = fromNode;
        this.paths = Paths.travel(fromNode);

        List<Path> routes = paths.leafs();

        // split paths into branches and find maxLookAhead required
        int maxLookAhead = 0;
        List<Path> branches[] = new List[paths.size()];
        for(Path route: routes){
            int branch = route.branch;
            if(branches[branch]==null)
                branches[branch] = new ArrayList<Path>();
            branches[branch].add(route);
            maxLookAhead = Math.max(maxLookAhead, route.depth);
        }
        this.maxLookAhead = maxLookAhead;

        // find branch with multiple paths
        int branchWithMultiplePaths = -1;
        for(int branch=0; branch<branches.length; branch++){
            if(branches[branch].size()>1){
                if(branchWithMultiplePaths==-1)
                    branchWithMultiplePaths = branch;
                else{
                    if(branches[branch].size()>branches[branchWithMultiplePaths].size())
                        branchWithMultiplePaths = branch;
                }
            }
        }

        if(branchWithMultiplePaths==-1)
            indeterminateRoute = null;
        else
            indeterminateRoute = branches[branchWithMultiplePaths].get(0);

        determinateRoutes = new ArrayList<Path>();
        for(int branch=0; branch<branches.length; branch++){
            if(branch!=branchWithMultiplePaths)
                determinateRoutes.addAll(branches[branch]);
        }
        Collections.sort(determinateRoutes, new Comparator<Path>(){
            @Override
            public int compare(Path route1, Path route2){
                int diff = route1.depth - route2.depth;
                if(diff==0){
                    if(route1.fallback())
                        return +1;
                    else if(route2.fallback())
                        return -1;
                    else
                        return 0;
                }else
                    return diff;
            }
        });

        Path routeStartingWithEOF = null;
        for(Path route: determinateRoutes){
            if(route.parent==null && route.matcher()==null){
                if(routeStartingWithEOF!=null)
                    throw new ImpossibleException("found more that one route starting with <EOF>");
                routeStartingWithEOF = route;
            }
        }
        if(routeStartingWithEOF!=null)
            determinateRoutes.remove(routeStartingWithEOF);
        this.routeStartingWithEOF = routeStartingWithEOF;
    }

    public Integer[] lookAheads(){
        TreeSet<Integer> set = new TreeSet<Integer>();
        for(Path route: determinateRoutes){
            set.add(route.depth);
        }
        return set.toArray(new Integer[set.size()]);
    }

    public List<Path> determinateRoutes(int lookAhead){
        List<Path> routes = new ArrayList<Path>();
        for(Path route: determinateRoutes){
            if(route.depth==lookAhead)
                routes.add(route);
        }
        return routes;
    }
    
    public String toString(){
        StringBuilder buff = new StringBuilder();
        for(Path route: determinateRoutes)
            add(buff, route);
        if(indeterminateRoute !=null)
            add(buff, indeterminateRoute.route()[0]);
        if(routeStartingWithEOF!=null)
            add(buff, routeStartingWithEOF);

        return buff.toString();
    }

    private void add(StringBuilder buff, Path route){
        if(buff.length()>0)
            buff.append(" OR ");
        buff.append(route);
    }

    public boolean isEOF(){
        if(maxLookAhead>1)
            return false;
        if(determinateRoutes.size()>0)
            return false;
        if(indeterminateRoute!=null)
            return false;
        for(Object obj: routeStartingWithEOF){
            if(obj instanceof Node){
                Node node = (Node)obj;
                if(node.action!=null)
                    return false;
            }else if(obj instanceof Edge){
                Edge edge = (Edge)obj;
                if(edge.ruleTarget!=null)
                    return false;
                else if(edge.matcher!=null)
                    return false;
            }
        }
        return true;
    }
}

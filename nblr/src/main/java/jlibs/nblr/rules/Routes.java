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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Routes{
    public final Node fromNode;
    public final Paths paths;
    public final int maxLookAhead;
    public final List<Path> determinateBranchRoutes;
    public final List<Path> indeterminateBranchRoutes;
    public final Path routeStartingWithEOF;

    @SuppressWarnings({"unchecked"})
    public Routes(Node fromNode){
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
            indeterminateBranchRoutes = new ArrayList<Path>();
        else
            indeterminateBranchRoutes = branches[branchWithMultiplePaths];

        determinateBranchRoutes = new ArrayList<Path>();
        for(int branch=0; branch<branches.length; branch++){
            if(branch!=branchWithMultiplePaths)
                determinateBranchRoutes.addAll(branches[branch]);
        }
        Collections.sort(determinateBranchRoutes, new Comparator<Path>(){
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
        for(Path route: determinateBranchRoutes){
            if(route.parent==null && route.matcher()==null){
                if(routeStartingWithEOF!=null)
                    throw new ImpossibleException("found more that one route starting with <EOF>");
                routeStartingWithEOF = route;
            }
        }
        if(routeStartingWithEOF!=null)
            determinateBranchRoutes.remove(routeStartingWithEOF);
        this.routeStartingWithEOF = routeStartingWithEOF;
    }

    public String toString(){
        StringBuilder buff = new StringBuilder();
        for(Path route: determinateBranchRoutes)
            add(buff, route);
        if(indeterminateBranchRoutes.size()>0)
            add(buff, indeterminateBranchRoutes.get(0).route()[0]);
        if(routeStartingWithEOF!=null)
            add(buff, routeStartingWithEOF);

        return buff.toString();
    }

    private void add(StringBuilder buff, Path route){
        if(buff.length()>0)
            buff.append(" OR ");
        buff.append(route);
    }
}

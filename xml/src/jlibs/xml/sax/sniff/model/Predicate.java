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

package jlibs.xml.sax.sniff.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Predicate{
    public Node parentNode;
    public List<Node> nodes = new ArrayList<Node>();
    public List<Predicate> predicates = new ArrayList<Predicate>();
    public List<Predicate> memberOf = new ArrayList<Predicate>();
    public boolean userGiven;

    @SuppressWarnings({"ManualArrayToCollectionCopy"})
    public Predicate(Node... nodes){
        for(Node node: nodes){
            // commented code needs be moved into xpathparser to support nested predicates
//            if(node.predicates.size()>0){
//                for(Predicate predicate: node.predicates){
//                    predicates.add(predicate);
//                    predicate.memberOf.add(this);
//                }
//            }else{
                this.nodes.add(node);
//                node.memberOf.add(this);
//            }
        }
    }

    public Predicate(Predicate predicate){
        predicates.add(predicate);
//        predicate.memberOf.add(this);
    }

    public boolean equivalent(Predicate predicate){
        return nodes.equals(predicate.nodes) && predicates.equals(predicate.predicates);
    }

    @Override
    public String toString(){
        StringBuilder buff1 = new StringBuilder();
        if(userGiven)
            buff1.append("userGiven ");
        for(Node node: nodes){
            buff1.append(", ");
            buff1.append(node);
        }

        for(Predicate predicate: predicates){
            if(buff1.length()>0)
                buff1.append(", ");
            buff1.append(predicate);
        }

//        if(memberOf.size()>0)
//            buff1.append("==> ");
//        for(Predicate predicate: memberOf){
//            if(buff1.length()>0)
//                buff1.append(", ");
//            buff1.append(predicate);
//        }

        return "["+buff1+"]";
    }
}

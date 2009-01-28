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

package jlibs.xml.sax.sniff.model.computed;

import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Results;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class ComputedResults extends Node{
    @Override
    public boolean equivalent(Node node){
        return false;
    }
    
    public List<Results> members = new ArrayList<Results>();

    public Iterable<Results> members(){
        return members;
    }

    public void addMember(Results member){
        root = ((Node)member).root;
        members.add(member);
        member.observers.add(this);
    }

    public abstract void memberHit(Results member, Context context, Event event);

    public String getName(){
        return getClass().getSimpleName();
    }

    protected void clearResults(){
        for(Results observer: members()){
            if(observer instanceof ComputedResults)
                ((ComputedResults)observer).clearResults();
        }
        for(ComputedResults observer: observers())
            observer.clearResults(this);
    }

    public void clearResults(Results member){}

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        if(userGiven)
            buff.append("UserGiven");
        for(Results member: members){
            if(buff.length()>0)
                buff.append(", ");
            buff.append('(').append(member).append(')');
        }
        return getName()+'{'+buff+'}';
    }
}

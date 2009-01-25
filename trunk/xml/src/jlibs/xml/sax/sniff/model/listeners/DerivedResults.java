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

package jlibs.xml.sax.sniff.model.listeners;

import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Results;
import jlibs.xml.sax.sniff.model.functions.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class DerivedResults extends Node{
    public List<Results> members = new ArrayList<Results>();

    public Iterable<Results> members(){
        return members;
    }
    
    public void addMember(Results member){
        members.add(member);
    }

    public DerivedResults attach(){
        for(Results member: members)
            member.listeners.add(this);
        return this;
    }

    @Override
    public boolean equivalent(Node node){
        return false;
    }

    protected String getResult(Results member){
        String result = null;
        if(member instanceof DerivedResults)
            ((DerivedResults)member).joinResults();
        else if(member instanceof Function)
            ((Function)member).joinResults();

        if(member.results!=null && member.results.size()>0)
            result = member.results.firstEntry().getValue();

        if(result==null && member instanceof Function)
            result = ((Function)member).defaultResult();
        
        return result;
    }

    public void joinResults(){}
}

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

import jlibs.xml.sax.sniff.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.computed.ComputedResults;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class UserResults extends Results{
    public boolean userGiven;
    public HitManager hits = new HitManager();

    public ResultType resultType(){
        return ResultType.NODESET;
    }

    @Override
    public void addResult(int docOrder, String result){
        super.addResult(docOrder, result);
        hits.hit();

        if(debug)
            System.out.format("Hit %2d: %s ---> %s %n", results.size(), this, result);
    }

    @Override
    public void reset(){
        super.reset();
        hits.reset();
    }

    public List<UserResults> listeners = new ArrayList<UserResults>();

    public Iterable<UserResults> listeners(){
        return listeners;
    }

    public void prepareResults(){}

    public boolean asBoolean(){
        return resultType().asBoolean(results);
    }

    public String asString(){
        return resultType().asString(results);
    }

    public double asNumber(){
        return resultType().asNumber(results);
    }

    public List<ComputedResults> observers = new ArrayList<ComputedResults>();

    public Iterable<ComputedResults> observers(){
        return observers;
    }

    public void notifyObservers(Context context, Event event){
        for(ComputedResults observer: observers())
            observer.memberHit(this, context, event);
    }

    public List<ComputedResults> cleanupObservers = new ArrayList<ComputedResults>();

    public Iterable<ComputedResults> cleanupObservers(){
        return cleanupObservers;
    }

    protected void notifyCleanupObservers(Context context){
        for(ComputedResults observer: cleanupObservers())
            observer.endingContext(context);
    }
}

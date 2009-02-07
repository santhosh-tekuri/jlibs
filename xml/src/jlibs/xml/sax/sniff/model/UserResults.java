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
public class UserResults extends Results{
    public int depth;
    public String xpath;
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
            debugger.println("Hit %d: %s ---> %s", results.size(), this, result);
    }

    public void userGiven(String xpath){
        userGiven = true;
        this.xpath = xpath;
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
}

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

package jlibs.wadl.cli.completors;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Buffer{
    private String str;
    private int from = 0;
    private List<String> candidates;

    public Buffer(String str, int cursor, List<String> candidates){
        this.str = str.substring(0, cursor);
        this.candidates = candidates;
    }

    private int separatorIndex;
    private String arg;
    
    public String next(){
        separatorIndex = str.indexOf(' ', from);
        if(separatorIndex==-1)
            arg = str.substring(from);
        else{
            arg = str.substring(from, separatorIndex);
            from = separatorIndex;
            while(str.charAt(from)==' '){
                from++;
                if(from==str.length())
                    break;
            }
        }
        return arg;
    }
    
    public int getFrom(){
        return candidates.isEmpty() ? -1 : from;
    }

    public void eat(int count){
        from += count;
        arg = arg.substring(count);
    }

    public boolean hasNext(){
        return separatorIndex!=-1;
    }
    
    public void addCandidate(String candidate){
        addCandidate(candidate, ' ');
    }

    public void addCandidate(String candidate, char separator){
        if(candidate.startsWith(arg))
            candidates.add(candidate+separator);
    }

    public void addCandidateIgnoreCase(String candidate){
        if(candidate.toLowerCase().startsWith(arg.toLowerCase()))
            candidates.add(candidate+' ');
    }
    
    public boolean hasCandidates(){
        return !candidates.isEmpty();
    }

    public List<String> candidates(){
        return candidates;
    }
}

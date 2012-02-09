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

import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
public class SetCompletion extends Completion{
    public SetCompletion(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public void complete(Buffer buffer){
        Path path = terminal.getCurrentPath();
        Deque<Path> stack = new ArrayDeque<Path>();
        while(path!=null){
            stack.push(path);
            path = path.parent;
        }

        Set<String> assigned = new LinkedHashSet<String>();
        Set<String> unAssigned = new LinkedHashSet<String>();
        while(!stack.isEmpty()){
            path = stack.pop();
            String var = path.variable();
            if(var!=null){
                if(path.value!=null)
                    assigned.add(var);
                else
                    unAssigned.add(var);
            }
        }

        while(true){
            String token = buffer.next();
            if(buffer.hasNext()){
                int equals = token.indexOf('=');
                if(equals!=-1){
                    String var = token.substring(0, equals);
                    unAssigned.remove(var);
                    assigned.remove(var);
                }
            }else{
                for(String var: unAssigned)
                    buffer.addCandidate(var, '=');
                if(!buffer.hasCandidates()){
                    for(String var: assigned)
                        buffer.addCandidate(var, '=');
                }
                return;
            }
        }
    }
}

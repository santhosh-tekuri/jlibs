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

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Completion{
    protected final WADLTerminal terminal;
    public Completion(WADLTerminal terminal){
        this.terminal = terminal;
    }

    public abstract void complete(Buffer buffer);

    protected void fillCandidates(List<String> candidates, String prefix, List<String> available){
        for(String arg: available){
            if(arg.toLowerCase().startsWith(prefix.toLowerCase()))
                candidates.add(arg+' ');
        }
    }
}

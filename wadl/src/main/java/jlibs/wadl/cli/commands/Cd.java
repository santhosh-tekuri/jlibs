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

package jlibs.wadl.cli.commands;

import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;

import java.util.List;

/**
 * @author Santhosh Kumar t
 */
public class Cd extends Command{
    public Cd(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public boolean run(String cmd, List<String> args) throws Exception{
        Path path = terminal.getCurrentPath();
        if(args.isEmpty())
            path = path.getRoot();
        else
            path = path.get(args.get(0));
        if(path==null){
            System.err.println("no such resource");
            return false;
        }else{
            terminal.setCurrentPath(path);
            return true;
        }
    }
}

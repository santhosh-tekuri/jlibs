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

import java.net.URL;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Target extends Command{
    public Target(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public boolean run(String cmd, List<String> args) throws Exception{
        if(args.isEmpty())
            terminal.getCurrentPath().getRoot().value = null;
        else{
            URL url = new URL(args.get(0));
            String target = url.getProtocol()+"://";
            target += url.getHost();
            if(url.getPort()!=-1)
                target += String.valueOf(url.getPort());
            terminal.getCurrentPath().getRoot().value = target;
        }
        return true;
    }
}

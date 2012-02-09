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
import java.util.Properties;

/**
 * @author Santhosh Kumar T
 */
public class Set extends Command{
    public Set(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public boolean run(String cmd, List<String> args) throws Exception{
        Properties vars = new Properties();
        for(String arg: args){
            int equals = arg.indexOf('=');
            if(equals!=-1){
                String var = arg.substring(0, equals);
                String value = arg.substring(equals+1);
                vars.setProperty(var, value);
            }
        }
        Path path = terminal.getCurrentPath();
        while(path!=null){
            String var = path.variable();
            if(var!=null){
                String value = vars.getProperty(var);
                if(value!=null)
                    path.value = value;
            }
            path = path.parent;
        }
        return true;
    }
}

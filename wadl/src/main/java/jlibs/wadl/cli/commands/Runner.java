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

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class Runner{
    private Map<String, Command> commands = new HashMap<String, Command>();
    private Command defaultCommand;

    public Runner(WADLTerminal terminal){
        commands.put("import", new Import(terminal));
        commands.put("cd", new Cd(terminal));
        commands.put("set", new Set(terminal));
        commands.put("server", new Server(terminal));
        commands.put("target", new Target(terminal));
        commands.put("authenticate", new Authenticate(terminal));
        defaultCommand = new jlibs.wadl.cli.commands.Method(terminal);
    }

    public boolean run(String command) throws Exception{
        List<String> args = getArguments(command);

        String arg1 = args.remove(0);
        Command cmd = commands.get(arg1);
        if(cmd!=null)
            return cmd.run(arg1, args);
        else
            return defaultCommand.run(arg1, args);
    }

    private List<String> getArguments(String command){
        List<String> args = new ArrayList<String>();
        StringTokenizer stok = new StringTokenizer(command, " ");
        while(stok.hasMoreTokens())
            args.add(stok.nextToken());
        return args;
    }






}

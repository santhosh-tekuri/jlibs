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

import jlibs.core.lang.NotImplementedException;
import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.commands.auth.BasicAuthenticator;

import java.io.IOException;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Authenticate extends Command{
    public Authenticate(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public boolean run(String cmd, List<String> args) throws Exception{
        if(args.size()==0)
            return false;
        String type = args.remove(0);

        if(type.equalsIgnoreCase("none")){
            terminal.getCurrentPath().getRoot().authenticator = null;
            return true;
        }else
            return authenticate(terminal, type, args);
    }
    
    public static boolean authenticate(WADLTerminal terminal, String type, List<String> args) throws IOException{
        if(type.equalsIgnoreCase(BasicAuthenticator.TYPE)){
            String user;
            if(!args.isEmpty())
                user = args.remove(0);
            else
                user = terminal.console.readLine("Login: ");
            if(user==null)
                return false;

            String passwd;

            if(!args.isEmpty())
                passwd = args.remove(0);
            else
                passwd = terminal.console.readLine("Password: ", (char)0);

            terminal.getCurrentPath().getRoot().authenticator = new BasicAuthenticator(user, passwd);
            return true;
        }else
                throw new NotImplementedException(type);
    }
}

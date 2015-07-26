/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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

/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

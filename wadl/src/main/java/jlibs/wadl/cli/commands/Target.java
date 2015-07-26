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

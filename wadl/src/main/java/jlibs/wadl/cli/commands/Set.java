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

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

package jlibs.wadl.cli.completors;

import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
public class SetCompletion extends Completion{
    public SetCompletion(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public void complete(Buffer buffer){
        Path path = terminal.getCurrentPath();
        Deque<Path> stack = new ArrayDeque<Path>();
        while(path!=null){
            stack.push(path);
            path = path.parent;
        }

        Set<String> assigned = new LinkedHashSet<String>();
        Set<String> unAssigned = new LinkedHashSet<String>();
        while(!stack.isEmpty()){
            path = stack.pop();
            String var = path.variable();
            if(var!=null){
                if(path.value!=null)
                    assigned.add(var);
                else
                    unAssigned.add(var);
            }
        }

        while(true){
            String token = buffer.next();
            if(buffer.hasNext()){
                int equals = token.indexOf('=');
                if(equals!=-1){
                    String var = token.substring(0, equals);
                    unAssigned.remove(var);
                    assigned.remove(var);
                }
            }else{
                for(String var: unAssigned)
                    buffer.addCandidate(var, '=');
                if(!buffer.hasCandidates()){
                    for(String var: assigned)
                        buffer.addCandidate(var, '=');
                }
                return;
            }
        }
    }
}

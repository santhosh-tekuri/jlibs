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

package jlibs.wadl.cli.completors;

import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;
import jlibs.wadl.model.Method;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class CommandCompletion extends Completion{
    private Map<String, Completion> completions = new HashMap<String, Completion>();
    private Completion defaultCompletion;
    public CommandCompletion(WADLTerminal terminal){
        super(terminal);
        completions.put("authenticate", new AuthenticateCompletion(terminal));
        completions.put("server", new ServerCompletion(terminal));
        completions.put("cd", new CdCompletion(terminal));
        completions.put("set", new SetCompletion(terminal));
        completions.put("import", new ImportCompletion(terminal));
        defaultCompletion = new MethodCompletion(terminal);
    }

    @Override
    public void complete(Buffer buffer){
        String arg = buffer.next();

        if(arg.isEmpty()){
            if(terminal.getCurrentPath()==null)
                buffer.addCandidate("import");
            else{
                buffer.addCandidate("cd");
                Path path = terminal.getCurrentPath();
                while(path!=null){
                    if(path.variable()!=null && path.value==null){
                        buffer.addCandidate("set");
                        break;
                    }
                    path = path.parent;
                }
                completeMethods(buffer);
            }
        }else if(!buffer.hasNext()){
            buffer.addCandidate("cd");
            buffer.addCandidate("authenticate");
            buffer.addCandidate("import");
            buffer.addCandidate("server");
            buffer.addCandidate("set");
            buffer.addCandidate("target");
            if(terminal.getCurrentPath()!=null){
                completeMethods(buffer);
                if(!buffer.hasCandidates()){
                    buffer.addCandidateIgnoreCase("GET");
                    buffer.addCandidateIgnoreCase("PUT");
                    buffer.addCandidateIgnoreCase("POST");
                    buffer.addCandidateIgnoreCase("DELETE");
                }
            }
        }else{
            Completion completion = completions.get(arg);
            if(completion==null)
                completion = defaultCompletion;
            completion.complete(buffer);
        }
    }

    private void completeMethods(Buffer buffer){
        Path path = terminal.getCurrentPath();
        if(path.resource!=null){
            for(Object obj: path.resource.getMethodOrResource()){
                if(obj instanceof Method){
                    String method = ((Method)obj).getName().toUpperCase();
                    buffer.addCandidateIgnoreCase(method);
                }
            }
        }
    }
}

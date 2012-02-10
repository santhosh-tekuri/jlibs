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
            completeMethods(buffer);
            if(!buffer.hasCandidates()){
                buffer.addCandidateIgnoreCase("GET");
                buffer.addCandidateIgnoreCase("PUT");
                buffer.addCandidateIgnoreCase("POST");
                buffer.addCandidateIgnoreCase("DELETE");
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

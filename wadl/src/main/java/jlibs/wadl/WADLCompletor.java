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

package jlibs.wadl;

import jlibs.core.lang.StringUtil;
import jlibs.wadl.model.Method;
import jlibs.wadl.runtime.Path;
import jline.Completor;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class WADLCompletor implements Completor{
    private WADLTerminal terminal;
    public WADLCompletor(WADLTerminal terminal){
        this.terminal = terminal;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int complete(String buffer, int cursor, List candidates){
        int from = 0;
        int to = findArgument(buffer, from, ' ');
        String arg = buffer.substring(from, Math.min(to, cursor));
        if(arg.isEmpty() || cursor<=to){
            List<String> available = new ArrayList<String>();
            available.add("cd");

            if(!arg.isEmpty() && "import".startsWith(arg))
                available.add("import");

            if(!arg.isEmpty() && "server".startsWith(arg))
                available.add("server");

            if(!arg.isEmpty() && "set".startsWith(arg))
                available.add("set");
            else{
                Path path = terminal.getCurrentPath();
                while(path!=null){
                    if(path.variable()!=null && path.value==null){
                        available.add("set");
                        break;
                    }
                    path = path.parent;
                }
            }

            if(!arg.isEmpty() && "target".startsWith(arg))
                available.add("target");

            Path path = terminal.getCurrentPath();
            if(path.resource!=null){
                for(Object obj: path.resource.getMethodOrResource()){
                    if(obj instanceof Method){
                        String method = ((Method)obj).getName().toUpperCase();
                        if(method.startsWith(arg.toUpperCase()))
                            available.add(method);
                    }
                }
            }

            fillCandidates(candidates, arg, available);
            if(candidates.isEmpty())
                available.addAll(Arrays.asList("GET", "PUT", "POST", "DELETE"));
            fillCandidates(candidates, arg, available);
            return from;
        }else{
            if(arg.equals("cd"))
                return completePath(buffer, cursor, candidates, terminal.getCurrentPath(), to);
            else if(arg.equals("set"))
                return completeVariable(buffer, cursor, candidates, terminal.getCurrentPath(), to);
            else if(arg.equals("server")){
                String token = buffer.substring(to+1, cursor);
                if(token.contains(" "))
                    return -1;
                Path currentRoot = terminal.getCurrentPath();
                if(currentRoot!=null)
                    currentRoot = currentRoot.getRoot();
                for(Path root: terminal.getRoots()){
                    if(root!=currentRoot && root.name.startsWith(token))
                        candidates.add(root.name+" ");
                }
                return candidates.isEmpty() ? -1 : to+1;
            }else{
                Path path = terminal.getCurrentPath();
                if(path.resource!=null){
                    Method method = null;
                    for(Object obj: path.resource.getMethodOrResource()){
                        if(obj instanceof Method){
                            String m = ((Method)obj).getName();
                            if(arg.equalsIgnoreCase(m)){
                                method = (Method)obj;
                                break;
                            }
                        }
                    }
                    if(method==null)
                        return -1;
                    return completePath(buffer, cursor, candidates, terminal.getCurrentPath(), to);
                }

                return -1;
            }
        }
    }
    
    private int findArgument(String buffer, int from, char separator){
        for(; from<buffer.length(); from++){
            if(buffer.charAt(from)==separator)
                return from;
        }
        return from;
    }
    
    private void fillCandidates(List<String> candidates, String token, List<String> args){
        for(String arg: args){
            if(arg.toLowerCase().startsWith(token.toLowerCase()))
                candidates.add(arg+' ');
        }
    }
    
    private void fillPathCandidates(List<String> candidates, String token, Path current){
        for(Path child: current.children){
            if(child.variable()!=null){
                candidates.clear();
                return;
            }
            if(child.name.startsWith(token)){
                if(child.children.isEmpty())
                    candidates.add(child.name+' ');
                else{
                    String candidate = child.name;
                    while(child.resource==null && child.children.size()==1){
                        child = child.children.get(0);
                        candidate += "/"+child.name;
                    }
                    candidate += child.children.isEmpty() ? ' ' : '/';
                    candidates.add(candidate);
                }
            }
        }
    }

    private int completePath(String buffer, int cursor, List<String>candidates, Path path, int to){
        if(path.children.isEmpty())
            return -1;

        int from = to+1;
        to = findArgument(buffer, from, '/');
        if(to<buffer.length()){
            assert buffer.charAt(to)=='/';
            String token = buffer.substring(from, to);
            Path child = null;
            if(token.equals(".."))
                child = path.parent;
            else{
                for(Path c: path.children){
                    if(c.variable()!=null || c.name.equals(token)){
                        child = c;
                        break;
                    }
                }
            }
            if(child==null)
                return -1;
            return completePath(buffer, cursor, candidates, child, to);
        }
        String arg = buffer.substring(from, Math.min(to, cursor));
        fillPathCandidates(candidates, arg, path);
        return from;
    }
    
    private int completeVariable(String buffer, int cursor, List<String> candidates, Path path, int to){
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

        String tokens[] = StringUtil.getTokens(buffer.substring(to+1, cursor), " ", true);
        for(String token: tokens){
            int equals = token.indexOf('=');
            if(equals!=-1){
                String var = token.substring(0, equals);
                unAssigned.remove(var);
                assigned.remove(var);
            }
        }

        String token = tokens.length==0 ? "" : tokens[tokens.length-1];
        int equals = token.indexOf('=');
        if(equals!=-1){
            if(buffer.charAt(cursor-1)==' ')
                token = "";
            else
                return -1;
        }
        for(String var: unAssigned){
            if(var.startsWith(token))
                candidates.add(var+'=');
        }
        if(candidates.isEmpty()){
            for(String var: assigned){
                if(var.startsWith(token))
                    candidates.add(var+'=');
            }
        }
        return candidates.isEmpty() ? -1 : cursor-token.length();
    }
}

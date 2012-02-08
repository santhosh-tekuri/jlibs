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
import jlibs.wadl.model.Param;
import jlibs.wadl.model.Representation;
import jlibs.wadl.model.Response;
import jlibs.wadl.runtime.Path;
import jlibs.xml.dom.DOMUtil;
import jline.Completor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.net.HttpURLConnection;
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

            if(!arg.isEmpty() && "authenticate".startsWith(arg))
                available.add("authenticate");

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
            if(arg.equals("authenticate")){
                String token = buffer.substring(to+1, cursor);
                List<String> available = new ArrayList<String>();
                available.add("basic");
                available.add("oauth");
                fillCandidates(candidates, token, available);
                return candidates.isEmpty() ? -1 : to+1;
            }else if(arg.equals("cd"))
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

    private List<String> fetchResourceNames(Path current){
        if(current.resource==null)
            return Collections.emptyList();
        
        for(Object item: current.resource.getMethodOrResource()){
            if(item instanceof Method){
                Method method = (Method)item;
                if(method.getName().equalsIgnoreCase("GET")){
                    for(Response response: method.getResponse()){
                        for(Representation rep: response.getRepresentation()){
                            if(Command.isXML(rep.getMediaType())){
                                for(Param param: rep.getParam()){
                                    if(param.getPath()!=null)
                                        return fetchResourceNames(current, method, param.getPath());
                                }
                            }
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }
    
    private List<String> fetchResourceNames(Path current, Method method, String xpath){
        try{
            HttpURLConnection con = current.execute(method, new HashMap<String, List<String>>(), null);
            if(con.getResponseCode()==200){
                Document doc = DOMUtil.newDocumentBuilder(true, false).parse(con.getInputStream());
                XPathExpression expr = XPathFactory.newInstance().newXPath().compile(xpath);
                NodeList nodeSet = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
                List<String> resourceNames = new ArrayList<String>();
                for(int i=0; i<nodeSet.getLength(); i++)
                    resourceNames.add(nodeSet.item(i).getTextContent());
                return resourceNames;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void fillPathCandidates(List<String> candidates, String token, Path current){
        for(Path child: current.children){
            if(child.variable()!=null){
//                candidates.clear();
                for(String resourceName: fetchResourceNames(current)){
                    if(resourceName.startsWith(token)){
                        char terminator = child.children.isEmpty() ? ' ' : '/';
                        candidates.add(resourceName+terminator);
                    }
                }
            }else if(child.name.startsWith(token)){
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
        int from = to+1;
        int slash = buffer.substring(from).lastIndexOf('/');
        if(slash!=-1){
            slash = from+slash;
            String pathString = buffer.substring(to+1, slash);
            path = path.get(pathString);
            from = slash+1;
        }
        if(path.children.isEmpty())
            return -1;

        String arg = buffer.substring(from, cursor);
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

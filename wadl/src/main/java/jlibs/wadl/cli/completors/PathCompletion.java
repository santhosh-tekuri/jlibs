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

import jlibs.wadl.cli.Util;
import jlibs.wadl.cli.WADLTerminal;
import jlibs.wadl.cli.model.Path;
import jlibs.wadl.model.Method;
import jlibs.wadl.model.Param;
import jlibs.wadl.model.Representation;
import jlibs.wadl.model.Response;
import jlibs.xml.dom.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class PathCompletion extends Completion{
    public PathCompletion(WADLTerminal terminal){
        super(terminal);
    }

    @Override
    public void complete(Buffer buffer){
        Path path = terminal.getCurrentPath();

        String pathString = buffer.next();
        int slash = pathString.lastIndexOf('/');
        if(slash!=-1){
            buffer.eat(slash+1);
            path = path.get(pathString.substring(0, slash));
            if(path==null)
                return;
            pathString = pathString.substring(slash+1);
        }
        if(path.children.isEmpty())
            return;

        if(buffer.hasNext()){
            path = path.get(pathString);
            completeNext(buffer, path);
        }else
            fillPathCandidates(buffer, path);
    }
    
    protected abstract void completeNext(Buffer buffer, Path path);

    private void fillPathCandidates(Buffer buffer, Path current){
        for(Path child: current.children){
            if(child.variable()!=null){
//                candidates.clear();
                for(String resourceName: fetchResourceNames(current)){
                    char terminator = child.children.isEmpty() ? ' ' : '/';
                    buffer.addCandidate(resourceName, terminator);
                }
            }else{
                if(child.children.isEmpty())
                    buffer.addCandidate(child.name, ' ');
                else{
                    String candidate = child.name;
                    while(child.resource==null && child.children.size()==1){
                        child = child.children.get(0);
                        candidate += "/"+child.name;
                    }
                    char separator = child.children.isEmpty() ? ' ' : '/';
                    buffer.addCandidate(candidate, separator);
                }
            }
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
                            if(Util.isXML(rep.getMediaType())){
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
//            if(con.getResponseCode()==401){
//                System.out.println();
//                new Command(terminal).authenticate(con);
//                return Collections.emptyList();
//            }
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
}

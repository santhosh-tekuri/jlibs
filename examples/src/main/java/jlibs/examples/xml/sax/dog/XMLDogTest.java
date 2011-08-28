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

package jlibs.examples.xml.sax.dog;

import jlibs.core.lang.OS;
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.dog.*;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.EvaluationListener;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.InstantEvaluationListener;
import jlibs.xml.sax.dog.sniff.DOMBuilder;
import jlibs.xml.sax.dog.sniff.Event;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XMLDogTest{
    public static void main(String[] args) throws Exception{
        boolean createDOM = false;
        boolean instantResults = false;
        String file = null;
        for(String arg: args){
            if("-dom".equals(arg))
                createDOM = true;
            if("-instantResults".equals(arg))
                instantResults = true;
            else
                file = arg;
        }

        if(file==null){
            System.out.println("usage: xmldog."+(OS.get().isWindows()?"bat":"sh")+" [-dom] -instantResults <xml-file>");
            System.exit(1);
        }

        System.out.println("Namespaces:");

        final DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        SAXUtil.newSAXParser(true, false, false).parse(new InputSource(file), new DefaultHandler(){
            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException{
                if(uri.length()>0 && prefix.length()==0)
                    prefix = "ns";
                if(nsContext.getPrefix(uri)==null){
                    String _uri = nsContext.getNamespaceURI(prefix);
                    if(_uri!=null && _uri.length()>0){
                        int i = 1;
                        String _prefix;
                        if(prefix.length()==0)
                            prefix = "ns";
                        while(true){
                            _prefix = prefix + i;
                            if(nsContext.getNamespaceURI(_prefix)==null){
                                prefix = _prefix;
                                break;
                            }
                            i++;
                        }
                    }
                    nsContext.declarePrefix(prefix, uri);
                    System.out.println(prefix+"\t= "+uri);
                }
            }
        });

        final XMLDog dog = new XMLDog(nsContext, null, null);
        List<Expression> expressions = new ArrayList<Expression>();

        System.out.println();
        System.out.println("XPaths: ");
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while((line=console.readLine())!=null){
            line = line.trim();
            if(line.startsWith("#for-each ")){
                int i = line.indexOf("#eval ");
                String forEach = line.substring("#for-each ".length(), i);
                String xpath = line.substring(i+"#eval ".length());
                expressions.add(dog.addForEach(forEach, xpath));
            }else if(line.length()>0)
                expressions.add(dog.addXPath(line));
            else
                break;
        }

        System.out.println("=========================================");
        System.out.println("|          XPath-Results                |");
        System.out.println("=========================================");
        System.out.println();
        long time = System.nanoTime();

        Event event = dog.createEvent();
        if(createDOM)
            event.setXMLBuilder(new DOMBuilder());

        EvaluationListener listener;
        if(instantResults){
            listener = new InstantEvaluationListener(){
                int nodeCounts[] = new int[dog.getDocumentXPathsCount()];
                @Override
                public void onNodeHit(Expression expression, NodeItem nodeItem){
                    System.out.print("XPath: "+expression.getXPath()+" Node["+ ++nodeCounts[expression.id]+"]: ");
                    nodeItem.printTo(System.out);
                    System.out.println();
                }

                @Override
                public void finished(Evaluation evaluation){
                    Object result = evaluation.getResult();
                    if(result==null)
                        System.out.println("Finished: "+evaluation.expression.getXPath());
                    else
                        XPathResults.print(System.out, evaluation.expression.getXPath(), result);
                    System.out.println();
                }
            };
            for(Expression expr: dog.getXPaths()){
                if(expr.scope()==Scope.DOCUMENT)
                    event.addListener(expr, listener);
                else{
                    Object result = expr.getResult();
                    if(expr.resultType==DataType.NODESET){
                        List<NodeItem> list = (List<NodeItem>)result;
                        if(list.size()==1 && list.get(0).type==NodeType.DOCUMENT)
                            continue;
                    }
                    XPathResults.print(System.out, expr.getXPath(), result);
                }
            }
        }else
            listener = new XPathResults(event, dog.getDocumentXPathsCount(), dog.getXPaths());
        dog.sniff(event, new InputSource(file));
        if(instantResults){
            for(Expression expr: dog.getXPaths()){
                if(expr.scope()!=Scope.DOCUMENT){
                    if(expr.resultType==DataType.NODESET){
                        List<NodeItem> list = (List<NodeItem>)expr.getResult();
                        if(list.size()==1 && list.get(0).type==NodeType.DOCUMENT)
                            XPathResults.print(System.out, expr.getXPath(), Collections.singletonList(event.documentNodeItem()));
                    }
                }
            }
        }
        time = System.nanoTime() - time;
        if(listener instanceof XPathResults)
            ((XPathResults)listener).print(expressions, System.out);
        System.out.println("Evaluated in "+(long)(time*1E-06)+" milliseconds");
    }
}

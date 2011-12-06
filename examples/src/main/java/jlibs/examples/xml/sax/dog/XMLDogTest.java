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
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
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
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XMLDogTest{
    public static void main(String[] args) throws Exception{
        boolean createDOM = false;
        boolean instantResults = false;
        final boolean printResults;

        boolean _printResults = true;
        String file = null;
        for(String arg: args){
            if("-dom".equals(arg))
                createDOM = true;
            if("-instantResults".equals(arg))
                instantResults = true;
            if("-dontPrintResults".equals(arg))
                _printResults = false;
            else
                file = arg;
        }
        printResults = _printResults;

        if(file==null){
            System.err.println("usage: xmldog."+(OS.get().isWindows()?"bat":"sh")+" [-dom] -instantResults -dontPrintResults <xml-file>");
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
        System.out.println("XPaths: (press CTRL+"+(OS.get().isUnix()?'D':'Z')+" after all xpaths are specified)");
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

        System.out.println("+----------------------------------------+");
        System.out.println("|              XPath-Results             |");
        System.out.println("+----------------------------------------+");
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
                    if(printResults){
                        System.out.print("XPath: "+expression.getXPath()+" Node["+ ++nodeCounts[expression.id]+"]: ");
                        nodeItem.printTo(System.out);
                        System.out.println();
                    }
                }

                @Override
                public void finishedNodeSet(Expression expression){
                    System.out.println("Finished Nodeset: "+expression.getXPath());
                }

                @Override
                public void onResult(Expression expression, Object result){
                    if(printResults){
                        XPathResults.print(System.out, expression.getXPath(), result);
                        System.out.println();
                    }
                }
            };
        }else
            listener = new XPathResults(event);
        event.setListener(listener);
        dog.sniff(event, new InputSource(file));
        time = System.nanoTime() - time;
        if(printResults && listener instanceof XPathResults)
            ((XPathResults)listener).print(expressions, System.out);
        System.err.println("Evaluated in "+(long)(time*1E-06)+" milliseconds");
        if(printResults && instantResults)
            System.err.println("Note: the above duration include the time to print results.");
    }
}

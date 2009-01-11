package jlibs.xml.sax.sniff; /**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.dom.DOMUtil;
import jlibs.xml.sax.SAXUtil;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XPathPerformanceTest{
    private String file;
    private List<String> xpaths = new ArrayList<String>();
    private DefaultNamespaceContext nsContext = new DefaultNamespaceContext();

    private Document doc;
    public List<List<String>> usingJDK() throws Exception{
        InputSource source = new InputSource(file);
        if(doc==null)
            doc = DOMUtil.newDocumentBuilder(true, false, false).parse(source);

        List<List<String>> results = new ArrayList<List<String>>(xpaths.size());
        for(String xpath: xpaths){
            XPath xpathObj = XPathFactory.newInstance().newXPath();
            xpathObj.setNamespaceContext(nsContext);
            NodeList nodeSet;
            if(doc!=null)
                nodeSet = (NodeList)xpathObj.evaluate(xpath, doc, XPathConstants.NODESET);
            else
                nodeSet = (NodeList)xpathObj.evaluate(xpath, source, XPathConstants.NODESET);

            List<String> result = new ArrayList<String>();
            for(int i=0; i<nodeSet.getLength(); i++){
                Node node = nodeSet.item(i);
                if(node instanceof Attr)
                    result.add(node.getNodeValue());
                else if(node instanceof Element)
                    result.add("true");
                else if(node instanceof Text)
                    result.add(node.getNodeValue());
            }
            results.add(result);
        }
        return results;
    }

    public List<List<String>> usingXMLDog() throws Exception{
        InputSource source = new InputSource(file);
        XMLDog dog = new XMLDog(nsContext);
        jlibs.xml.sax.sniff.XPath xpathObjs[] = new jlibs.xml.sax.sniff.XPath[xpaths.size()];
        for(int i=0; i<xpaths.size(); i++)
            xpathObjs[i] = dog.add(xpaths.get(i));

        XPathResults dogResults = dog.sniff(source);

        List<List<String>> results = new ArrayList<List<String>>(xpaths.size());
        for(jlibs.xml.sax.sniff.XPath xpathObj: xpathObjs)
            results.add(dogResults.getResult(xpathObj));
        return results;
    }

    public void run(String configFile) throws Exception{
        SAXUtil.newSAXParser(true, false).parse(configFile, new DefaultHandler(){
            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException{
                nsContext.declarePrefix(prefix, uri);
            }

            CharArrayWriter contents = new CharArrayWriter();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
                contents.reset();
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException{
                contents.write(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException{
                if(localName.equals("file"))
                    file = contents.toString().trim();
                else if(localName.equals("xpath"))
                    xpaths.add(contents.toString().trim());

            }
        });

        int total=0, failed=0;
        long time = System.nanoTime();
        List<List<String>> jdkResults = usingJDK();
        long jdkTime = System.nanoTime() - time;
        time = System.nanoTime();
        List<List<String>> dogResults = usingXMLDog();
        long dogTime = System.nanoTime() - time;

        PrintStream stream = System.out;

        for(int i=0; i<xpaths.size(); i++){
            total++;

            List<String> jdkResult = jdkResults.get(i);
            List<String> dogResult = dogResults.get(i);

            if(xpaths.get(i).contains("@")){
                Collections.sort(jdkResult);
                Collections.sort(dogResult);
            }

            boolean matched = jdkResult.equals(dogResult);

            System.out.println(matched ? "SUCCESSFULL:" : "FAILED:");
            if(!matched)
                failed++;

            stream.println("         xpath : "+xpaths.get(i));
            stream.println("    jdk result : "+jdkResult);
            stream.println("  jdk hitcount : "+jdkResult.size());
            stream.println("    dog result : "+dogResult);
            stream.println("  dog hitcount : "+dogResult.size());
            stream.flush();

            System.out.println("-------------------------------------------------");
            System.out.flush();
        }
        System.out.format("testcases are executed: total=%d failed=%d %n", total, failed);

        stream.println("      jdk time : "+jdkTime+" nanoseconds");
        stream.println("      dog time : "+dogTime+" nanoseconds");
        stream.println("        WINNER : "+(dogTime<=jdkTime ? "XMLDog" : "XALAN"));
        long diff = Math.abs(dogTime - jdkTime);
        stream.println("    Difference : "+ diff + " nanoseconds/"+ diff/1E-09+" seconds");

        System.out.println("\n--------------------------------------------------------------\n\n");
    }

    public static void main(String[] args) throws Exception{
        new XPathPerformanceTest().run(args.length==0 ? "xpaths.xml" : args[0]);
    }
}
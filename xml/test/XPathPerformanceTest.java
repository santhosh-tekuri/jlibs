/**
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
import jlibs.xml.Namespaces;
import jlibs.xml.dom.DOMUtil;
import jlibs.xml.sax.sniff.XMLDog;
import jlibs.xml.sax.sniff.XPathResults;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * @author Santhosh Kumar T
 */
public class XPathPerformanceTest{
    private String file;
    private String xpaths[];
    private DefaultNamespaceContext nsContext;

    private Document doc;
    public List<List<String>> usingJDK() throws Exception{
        InputSource source = new InputSource(file);
        if(doc==null)
            doc = DOMUtil.newDocumentBuilder(true, false, false).parse(source);

        List<List<String>> results = new ArrayList<List<String>>(xpaths.length);
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
                org.w3c.dom.Node node = nodeSet.item(i);
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
        jlibs.xml.sax.sniff.XPath xpathObjs[] = new jlibs.xml.sax.sniff.XPath[xpaths.length];
        for(int i=0; i<xpaths.length; i++)
            xpathObjs[i] = dog.add(xpaths[i]);

        XPathResults dogResults = dog.sniff(source);

        List<List<String>> results = new ArrayList<List<String>>(xpaths.length);
        for(jlibs.xml.sax.sniff.XPath xpathObj: xpathObjs)
            results.add(dogResults.getResult(xpathObj));
        return results;
    }

    public void run() throws Exception{
        String inputs[][] = {
            {
                getClass().getResource("test.xml").toString(),
                "xs="+Namespaces.URI_XSD,
                "abc=http://www.w3schools.com",
                null,
                "/xs:schema/@targetNamespace",
                "/xs:schema/xs:complexType",
                "/xs:schema/xs:complexType/@name",
                "/xs:schema/*/@name",
                "/xs:schema/xs:*/@name",
                "xs:schema//xs:element/@name",
                "/xs:schema/descendant-or-self::xs:element/@name",
                "/xs:schema/descendant-or-self::xs:schema/@targetNamespace",
                "/xs:schema//@name",
                "/xs:schema//text()",
                "/xs:schema/@*",
                "/xs:schema/@abc:*",
                "/xs:schema/*/xs:complexType/@name",
                "//xs:any[2]/@namespace",
                "//@name",
                "xs:schema//xs:complexType/@name",
                "xs:schema/xs:any/@namespace",
                "//xs:sequence/child::xs:any/@namespace",
            }
        };
        int total=0, failed=0;
        for(String input[]: inputs){
            file = input[0];
            nsContext = new DefaultNamespaceContext();
            int i = 1;
            while(i<input.length && input[i]!=null){
                int equals = input[i].indexOf("=");
                nsContext.declarePrefix(input[i].substring(0, equals), input[i].substring(equals+1));
                i++;
            }
            i++;

            xpaths = new String[input.length-i];
            System.arraycopy(input, i, xpaths, 0, xpaths.length);

            long time = System.nanoTime();
            List<List<String>> jdkResults = usingJDK();
            long jdkTime = System.nanoTime() - time;
            time = System.nanoTime();
            List<List<String>> dogResults = usingXMLDog();
            long dogTime = System.nanoTime() - time;

            PrintStream stream = System.out;

            for(int j=0; j<xpaths.length; j++){
                total++;

                List<String> jdkResult = jdkResults.get(j);
                List<String> dogResult = dogResults.get(j);

                if(xpaths[j].contains("@")){
                    Collections.sort(jdkResult);
                    Collections.sort(dogResult);
                }
                
                boolean matched = jdkResult.equals(dogResult);

                System.out.println(matched ? "SUCCESSFULL:" : "FAILED:");
                if(!matched)
                    failed++;

                stream.println("         xpath : "+xpaths[j]);
                stream.println("    jdk result : "+jdkResult);
                stream.println("  jdk hitcount : "+jdkResult.size());
                stream.println("    dog result : "+dogResult);
                stream.println("  dog hitcount : "+dogResult.size());
                stream.flush();
                i++;
                System.out.println("-------------------------------------------------");
                System.out.flush();
            }
            System.out.format("testcases are executed: total=%d failed=%d %n", total, failed);

            stream.println("      jdk time : "+jdkTime);
            stream.println("      dog time : "+dogTime);
            stream.println("        WINNER : "+(dogTime<=jdkTime ? "XMLDog" : "XALAN"));
            long diff = Math.abs(dogTime - jdkTime);
            stream.println("    Difference : "+ diff + " nanoseconds/"+ diff/1E-09+" seconds");

            System.out.println("\n--------------------------------------------------------------\n\n");
        }
    }

    public static void main(String[] args) throws Exception{
        new XPathPerformanceTest().run();
    }
}
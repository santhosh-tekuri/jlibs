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
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XPathTest{
    private String file;
    private String xpath;
    private DefaultNamespaceContext nsContext;

    private Document doc;
    public List<String> usingJDK() throws Exception{
        InputSource source = new InputSource(file);
        if(doc==null)
            doc = DOMUtil.newDocumentBuilder(true, false, false).parse(source);

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
        }
        return result;
    }

    public List<String> usingXMLDog() throws Exception{
        InputSource source = new InputSource(file);
        XMLDog dog = new XMLDog(nsContext);
        jlibs.xml.sax.sniff.XPath xpathObj = dog.add(xpath);
        XPathResults results = dog.sniff(source);
        return results.getResult(xpathObj);
    }

    public void run() throws Exception{
        String inputs[][] = {
            {
                getClass().getResource("test.xml").toString(),
                "xs="+Namespaces.URI_XSD,
                "abc=http://www.w3schools.com",
                null,
                "xs:schema/@targetNamespace",
                "xs:schema/xs:element/xs:complexType/@name",
                "xs:schema//xs:element/@name",
                "xs:schema/*/@name",
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
            while(i<input.length){
                total++;
                xpath = input[i];
                long time = System.nanoTime();
                List<String> jdkResult = usingJDK();
                long jdkTime = System.nanoTime() - time;
                time = System.nanoTime();
                List<String> dogResult = usingXMLDog();
                long dogTime = System.nanoTime() - time;

                boolean matched = jdkResult.equals(dogResult);

                PrintStream stream = System.out;
                if(matched){
                    System.out.println("SUCCESSFULL:");
                    System.out.flush();
                }else{
                    stream = System.err;
                    System.err.println("FAILED:");
                    failed++;
                }

                stream.println("         xpath : "+xpath);
                stream.println("    jdk result : "+jdkResult);
                stream.println("  jdk hitcount : "+jdkResult.size());
                stream.println("      jdk time : "+jdkTime);
                stream.println("    dog result : "+dogResult);
                stream.println("      dog time : "+dogTime);
                stream.println("        WINNER : "+(dogTime<=jdkTime ? "XMLDog" : "Xalan"));
                stream.println("    Difference : "+(dogTime-jdkTime));
                stream.flush();
                i++;
                System.out.println("-------------------------------------------------");
                System.out.flush();
            }
            System.out.format("testcases are executed: total=%d failed=%d %n", total, failed);
        }
    }

    public static void main(String[] args) throws Exception{
        new XPathTest().run();
    }
}

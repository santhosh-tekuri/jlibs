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
import jlibs.xml.sax.sniff.Node;
import jlibs.xml.sax.sniff.XMLDog;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
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

    public List<String> usingJDK() throws Exception{
        InputSource source = new InputSource(file);
        XPath xpathObj = XPathFactory.newInstance().newXPath();
        xpathObj.setNamespaceContext(nsContext);
        NodeList nodeSet = (NodeList)xpathObj.evaluate(xpath, source, XPathConstants.NODESET);

        List<String> result = new ArrayList<String>();
        for(int i=0; i<nodeSet.getLength(); i++){
            org.w3c.dom.Node node = nodeSet.item(i);
            if(node instanceof Attr)
                result.add(node.getNodeValue());
        }
        return result;
    }

    public Node usingXMLDog() throws Exception{
        InputSource source = new InputSource(file);
        XMLDog dog = new XMLDog(nsContext);
        Node node = dog.add(xpath);
        dog.sniff(source);
        return node;
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
                List<String> jdkResult = usingJDK();
                Node node = usingXMLDog();

                boolean matched;
                if(node.getHitCount()==node.getResult().size())
                    matched = jdkResult.equals(node.getResult());
                else
                    matched = jdkResult.size()==node.getHitCount();

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
                stream.println("    dog result : "+node.getResult());
                stream.println("  dog hitcount : "+node.getHitCount());
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

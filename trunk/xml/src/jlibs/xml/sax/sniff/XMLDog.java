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

package jlibs.xml.sax.sniff;

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.sniff.model.Root;
import org.jaxen.saxpath.SAXPathException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XMLDog{
    public static boolean debug = false;

    private Root root;

    public XMLDog(NamespaceContext nsContext){
        root = new Root(nsContext);
    }

    public XPath add(String xpath) throws SAXPathException{
        return add(xpath, -1);
    }

    private boolean infiniteHits;
    private int totalMinHits;
    public XPath add(String xpath, int minHits) throws SAXPathException{
        if(!xpath.startsWith("/"))
            xpath = "/"+xpath;

        XPath compiledXPath = new XPathParser(root).parse(xpath);
        compiledXPath.minHits = minHits;

        if(minHits<0)
            infiniteHits = true;
        if(!infiniteHits)
            totalMinHits += minHits;

        return compiledXPath;
    }

    public XPathResults sniff(InputSource source) throws ParserConfigurationException, SAXException, IOException{
        return new Sniffer(root).sniff(source, infiniteHits ? -1 : totalMinHits);
    }

    public static void main(String[] args) throws Exception{
        XMLDog.debug = false;

        DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        nsContext.declarePrefix("xs", Namespaces.URI_XSD);
        nsContext.declarePrefix("abc", "http://abc.com");

        XMLDog dog = new XMLDog(nsContext);

        XPath xpaths[] = {
//              dog.add("/xs:schema/@targetNamespace", 1),
//              dog.add("/xs:schema/xs:complexType", -1),
//              dog.add("/xs:schema/xs:complexType/@name", -1),
//              dog.add("/xs:schema/*/@name", -1),
//              dog.add("/xs:schema/xs:*/@name", -1),
//              dog.add("xs:schema//xs:element/@name", -1),
//            dog.add("/xs:schema/descendant-or-self::xs:element/@name"),
//            dog.add("/xs:schema/descendant-or-self::xs:schema/@targetNamespace"),
//            dog.add("/xs:schema//@name"),
//            dog.add("/xs:schema//text()", -1),
//            dog.add("/xs:schema/@*", -1),
//            dog.add("/xs:schema/@abc:*", -1),
//            dog.add("/xs:schema/*/xs:complexType/@name", -1),
//            dog.add("//xs:any[2]/@namespace", -1),
//            dog.add("//@name"),
//            dog.add("xs:schema//xs:complexType/@name", 10),
//            dog.add("xs:schema/xs:any/@namespace", 10),
//            dog.add("//xs:sequence/child::xs:any/@namespace")
        };
        InputSource source = new InputSource("/Volumes/Softwares/Personal/jlibs/wiki/src/wiki/dog/test.xml");
        XPathResults results = dog.sniff(source);

        System.out.println("\n\nResults:");
        for(XPath xpath: xpaths){
            System.out.println("---------------------------------------");
            System.out.println("XPath: "+xpath);
            List<String> result = results.getResult(xpath);
            for(int i=0; i<result.size(); i++)
                System.out.format("      %02d: %s %n",i+1, result.get(i));
        }
    }

}

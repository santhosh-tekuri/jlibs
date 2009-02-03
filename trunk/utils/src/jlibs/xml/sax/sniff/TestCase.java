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

import jlibs.core.lang.NotImplementedException;
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.dom.DOMUtil;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class TestCase{
    String file;
    List<String> xpaths = new ArrayList<String>();
    List<QName> resultTypes = new ArrayList<QName>();
    List<Boolean> hasAttributes = new ArrayList<Boolean>();
    DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
    Document doc;

    public void createDocument() throws ParserConfigurationException, IOException, SAXException{
        doc = DOMUtil.newDocumentBuilder(true, false).parse(new InputSource(file));
    }

    List<Object> jdkResult;
    public List<Object> usingJDK() throws Exception{
        if(doc==null)
            createDocument();

        List<Object> results = new ArrayList<Object>(xpaths.size());
        for(int i=0; i<xpaths.size(); i++){
            javax.xml.xpath.XPath xpathObj = XPathFactory.newInstance().newXPath();
            xpathObj.setNamespaceContext(nsContext);
            results.add(xpathObj.evaluate(xpaths.get(i), doc, resultTypes.get(i)));
        }

        doc = null;
        System.out.print('.');
        return results;
    }

    List<List<String>> dogResult;
    public List<List<String>> usingXMLDog() throws Exception{
        InputSource source = new InputSource(file);
        XMLDog dog = new XMLDog(nsContext);
        jlibs.xml.sax.sniff.XPath xpathObjs[] = new jlibs.xml.sax.sniff.XPath[xpaths.size()];
        for(int i=0; i<xpaths.size(); i++){
            xpathObjs[i] = dog.add(xpaths.get(i));
            resultTypes.add(xpathObjs[i].resultType());
        }

        XPathResults dogResults = dog.sniff(source);

        List<List<String>> results = new ArrayList<List<String>>(xpaths.size());
        for(jlibs.xml.sax.sniff.XPath xpathObj: xpathObjs)
            results.add(dogResults.getResult(xpathObj));
        System.out.print('.');
        return results;
    }

    List<List<String>> translatedJDKResult = new ArrayList<List<String>>();
    public void translateJDKResults(int test){
        Object obj = jdkResult.get(test);
        hasAttributes.add(false);
        List<String> result = new ArrayList<String>();

        if(obj instanceof NodeList){
            NodeList nodeSet = (NodeList)obj;
            for(int i=0; i<nodeSet.getLength(); i++){
                Node node = nodeSet.item(i);
                if(node instanceof Attr){
                    result.add(node.getNodeValue());
                    hasAttributes.set(test, true);
                }else if(node instanceof Element){
                    StringBuilder buff = new StringBuilder();
                    while(!(node instanceof Document)){
                        String prefix = nsContext.getPrefix(node.getNamespaceURI());
                        buff.insert(0, "["+DOMUtil.getPosition((Element)node)+"]");
                        buff.insert(0, prefix.length()==0 ? node.getLocalName() : prefix+':'+node.getLocalName());
                        buff.insert(0, '/');
                        node = node.getParentNode();
                    }
                    result.add(buff.toString());
                }else if(node instanceof Text)
                    result.add(node.getNodeValue());
                else if(node instanceof Comment)
                    result.add(node.getNodeValue());
                else if(node instanceof ProcessingInstruction){
                    ProcessingInstruction pi = (ProcessingInstruction)node;
                    result.add(String.format("<?%s %s?>", pi.getTarget(), pi.getData()));
                }else if(node instanceof Document)
                    result.add("/");
                else
                    throw new NotImplementedException(node.getClass().getName());
            }
        }else if(obj instanceof String)
            result.add((String)obj);
        else if(obj instanceof Double)
            result.add(obj.toString());
        else if(obj instanceof Boolean)
            result.add(obj.toString());
        else
            throw new NotImplementedException(obj.getClass().getName());
        translatedJDKResult.add(result);
        jdkResult.set(test, null);
    }

    public List<String> jdkResults(int i){
        if(i>=translatedJDKResult.size())
            translateJDKResults(i);
        return translatedJDKResult.get(i);
    }

    public List<String> dogResults(int i){
        return dogResult.get(i);
    }

    public boolean passed(int i){
        List<String> jdkResults = jdkResults(i);
        List<String> dogResults = dogResults(i);

        if(hasAttributes.get(i)){
            Collections.sort(jdkResults);
            Collections.sort(dogResults);
        }

        return jdkResults.equals(dogResults);
    }

    /*-------------------------------------------------[ Printing ]---------------------------------------------------*/

    public static void printResults(List<String> results){
        boolean first = true;
        for(String result: results){
            if(first)
               first = false;
            else
                System.out.print(", ");
            System.out.print(result);
        }
        System.out.println();
    }

    public void printResults(int i){
        System.out.println("         xpath : "+xpaths.get(i));
        System.out.print("    jdk result : ");
        printResults(translatedJDKResult.get(i));
        System.out.println("  jdk hitcount : "+translatedJDKResult.get(i).size());
        translatedJDKResult.set(i, null);

        System.out.print("    dog result : ");
        printResults(dogResult.get(i));
        System.out.println("  dog hitcount : "+dogResult.get(i).size());
        dogResult.set(i, null);
        
        System.out.println("-------------------------------------------------");
    }
}
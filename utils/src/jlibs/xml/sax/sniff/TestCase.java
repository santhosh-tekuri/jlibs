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
    List<Integer> hasAttributes = new ArrayList<Integer>();
    DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
    Document doc;

    public void createDocument() throws ParserConfigurationException, IOException, SAXException{
        doc = DOMUtil.newDocumentBuilder(true, false).parse(new InputSource(file));
    }

    List<Object> jdkResult = new ArrayList<Object>(xpaths.size());
    public List<Object> usingJDK() throws Exception{
        if(doc==null)
            createDocument();

        jdkResult = new ArrayList<Object>(xpaths.size());
        for(int i=0; i<xpaths.size(); i++){
            javax.xml.xpath.XPath xpathObj = XPathFactory.newInstance().newXPath();
            xpathObj.setNamespaceContext(nsContext);
            jdkResult.add(xpathObj.evaluate(xpaths.get(i), doc, resultTypes.get(i)));
        }

        doc = null;
        return jdkResult;
    }

    List<Object> dogResult;
    public List<Object> usingXMLDog() throws Exception{
        InputSource source = new InputSource(file);
        XMLDog dog = new XMLDog(nsContext);
        jlibs.xml.sax.sniff.XPath xpathObjs[] = new jlibs.xml.sax.sniff.XPath[xpaths.size()];
        for(int i=0; i<xpaths.size(); i++){
            xpathObjs[i] = dog.add(xpaths.get(i));
            resultTypes.add(xpathObjs[i].resultType());
        }

        XPathResults dogResults = dog.sniff(source);

        dogResult = new ArrayList<Object>(xpaths.size());
        for(jlibs.xml.sax.sniff.XPath xpathObj: xpathObjs)
            dogResult.add(dogResults.getResult(xpathObj));
        return dogResult;
    }

    public void translateJDKResult(int test){
        Object obj = jdkResult.get(test);
        List<String> result = new ArrayList<String>();

        if(obj instanceof NodeList){
            NodeList nodeSet = (NodeList)obj;
            for(int i=0; i<nodeSet.getLength(); i++){
                Node node = nodeSet.item(i);
                if(node instanceof Attr){
                    result.add(node.getNodeValue());
                    hasAttributes.add(test);
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
        jdkResult.set(test, result);
    }

    public Object jdkResults(int i){
        if(jdkResult.get(i) instanceof NodeList)
            translateJDKResult(i);
        return jdkResult.get(i);
    }

    public Object dogResults(int i){
        return dogResult.get(i);
    }

    @SuppressWarnings({"unchecked"})
    public boolean passed(int i){
        Object jdkResults = jdkResults(i);
        Object dogResults = dogResults(i);

        if(hasAttributes.contains(i)){
            Collections.sort((List<String>)jdkResults);
            Collections.sort((List<String>)dogResults);
        }

        return jdkResults.equals(dogResults);
    }

    /*-------------------------------------------------[ Printing ]---------------------------------------------------*/

    @SuppressWarnings({"unchecked"})
    public static int printResults(Object result){
        if(result instanceof List)
            return printResults((List<String>)result);
        else{
            System.out.println(result);
            return -1;
        }
    }

    public static int printResults(List<String> results){
        boolean first = true;
        for(String result: results){
            if(first)
               first = false;
            else
                System.out.print(", ");
            System.out.print(result);
        }
        System.out.println();
        return results.size();
    }

    public void printResults(int i){
        System.out.println("          file : "+file);
        System.out.println("         xpath : "+xpaths.get(i));
        System.out.print("    jdk result : ");
        int count = printResults(jdkResults(i));
        if(count!=-1)
            System.out.println(" jdk hit-count : "+count);
        jdkResult.set(i, null);

        System.out.print("    dog result : ");
        count = printResults(dogResults(i));
        if(count!=-1)
            System.out.println(" dog hit-count : "+count);
        dogResult.set(i, null);
        
        System.out.println("-------------------------------------------------");
    }
}
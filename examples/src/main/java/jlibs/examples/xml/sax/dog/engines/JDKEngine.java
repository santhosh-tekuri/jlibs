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

package jlibs.examples.xml.sax.dog.engines;

import jlibs.examples.xml.sax.dog.TestCase;
import jlibs.examples.xml.sax.dog.XPathEngine;
import jlibs.examples.xml.sax.dog.XPathInfo;
import net.sf.saxon.xpath.JAXPXPathStaticContext;
import net.sf.saxon.xpath.XPathEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.transform.SourceLocator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Santhosh Kumar T
 */
public class JDKEngine extends XPathEngine{
    private final XPathFactory factory;

    public JDKEngine(XPathFactory factory){
        this.factory = factory;
    }

    @Override
    public String getName(){
        String name = factory.getClass().getName();
        if(name.contains("saxon"))
            return "Saxon";
        else if(name.contains("apache"))
            return "Xalan";
        else
            return name;
    }

    @Override
    public List<Object> evaluate(TestCase testCase, String file) throws Exception{
        Document doc = toDOM(file);
        List<Object> results = new ArrayList<Object>(testCase.xpaths.size());
        XPath xpathObj = factory.newXPath();
        if(xpathObj instanceof XPathEvaluator){
            XPathEvaluator xpe = (XPathEvaluator)xpathObj;
            xpe.setStaticContext(new JAXPXPathStaticContext(xpe.getConfiguration()){
                @Override public void issueWarning(String s, SourceLocator locator){}
            });
            xpe.setBackwardsCompatible(true);
        }
        for(XPathInfo xpathInfo: testCase.xpaths){
            xpathObj.setXPathVariableResolver(testCase.variableResolver);
            xpathObj.setXPathFunctionResolver(testCase.functionResolver);
            xpathObj.setNamespaceContext(testCase.nsContext);

            if(xpathInfo.forEach==null)
                results.add(xpathObj.evaluate(xpathInfo.xpath, doc, xpathInfo.resultType));
            else{
                List<Object> list = new ArrayList<Object>();
                NodeList nodeList = (NodeList)xpathObj.evaluate(xpathInfo.forEach, doc, XPathConstants.NODESET);
                for(int i=0; i<nodeList.getLength(); i++){
                    Object context = nodeList.item(i);
                    list.add(xpathObj.evaluate(xpathInfo.xpath, context, xpathInfo.resultType));
                }
                results.add(list);
            }
        }
        return results;
    }
}

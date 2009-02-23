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

import net.sf.saxon.xpath.XPathEvaluator;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Santhosh Kumar T
 */
public class JDKEngine extends XPathEngine{
//    XPathFactory factory = new com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl();
//    XPathFactory factory = new org.apache.xpath.jaxp.XPathFactoryImpl();
    XPathFactory factory = new net.sf.saxon.xpath.XPathFactoryImpl();

    @Override
    public String getName(){
        return "Xalan";
    }

    @Override
    public List<Object> evaluate(TestCase testCase, Document doc) throws Exception{
        List<Object> results = new ArrayList<Object>(testCase.xpaths.size());
        for(int i=0; i<testCase.xpaths.size(); i++){
            javax.xml.xpath.XPath xpathObj = factory.newXPath();
            if(xpathObj instanceof XPathEvaluator)
                ((XPathEvaluator)xpathObj).setBackwardsCompatible(true);

            xpathObj.setXPathVariableResolver(testCase.variableResolver);
            xpathObj.setNamespaceContext(testCase.nsContext);
            results.add(xpathObj.evaluate(testCase.xpaths.get(i), doc, testCase.resultTypes.get(i)));
        }
        return results;
    }
}

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

package jlibs.xml.sax.dog.engines;

import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import jlibs.xml.sax.dog.XPathEngine;
import jlibs.xml.sax.dog.TestCase;

/**
 * @author Santhosh Kumar T
 */
public class JaxenEngine extends XPathEngine{
    @Override
    public String getName(){
        return "Jaxen";
    }

    @Override
    public List<Object> evaluate(final TestCase testCase, String file) throws Exception{
        Document doc = toDOM(file);
        org.jaxen.NamespaceContext jaxenNSContext = new org.jaxen.NamespaceContext(){
            @Override
            public String translateNamespacePrefixToUri(String prefix){
                return testCase.nsContext.getNamespaceURI(prefix);
            }
        };

        VariableContext variableContext = new VariableContext(){
            @Override
            public Object getVariableValue(String namespaceURI, String prefix, String localName) throws UnresolvableException{
                return testCase.variableResolver.resolveVariable(new QName(namespaceURI, localName));
            }
        };

        List<Object> results = new ArrayList<Object>(testCase.xpaths.size());
        for(String xpath : testCase.xpaths){
            DOMXPath xpathObj = new DOMXPath(xpath);
            xpathObj.setVariableContext(variableContext);
            xpathObj.setNamespaceContext(jaxenNSContext);
            results.add(xpathObj.evaluate(doc));
        }
        return results;
    }
}

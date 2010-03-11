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

package jlibs.xml.sax.dog.engines;

import jlibs.xml.ClarkName;
import jlibs.xml.sax.dog.TestCase;
import jlibs.xml.sax.dog.XPathEngine;
import org.jaxen.*;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import java.util.ArrayList;
import java.util.List;

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
                Object value = testCase.variableResolver.resolveVariable(new QName(namespaceURI, localName));
                if(value==null)
                    throw new UnresolvableException("Unresolvable Variable: "+ ClarkName.valueOf(namespaceURI, localName));
                return value;
            }
        };
        
        FunctionContext functionContext = new FunctionContext() {
            @Override
            public Function getFunction(String namespaceURI, String prefix, String localName) throws UnresolvableException{
                final XPathFunction function = testCase.functionResolver.resolveFunction(new QName(namespaceURI, localName), 0);
                if(function==null)
                    throw new UnresolvableException("Unresolvable function: "+ ClarkName.valueOf(namespaceURI, localName));
                return new Function() {
                    @Override
                    public Object call(Context context, List list) throws FunctionCallException{
                        try{
                            return function.evaluate(list);
                        }catch (XPathFunctionException ex){
                            throw new FunctionCallException(ex);
                        }
                    }
                };
            }
        };

        List<Object> results = new ArrayList<Object>(testCase.xpaths.size());
        for(String xpath : testCase.xpaths){
            DOMXPath xpathObj = new DOMXPath(xpath);
            xpathObj.setVariableContext(variableContext);
            xpathObj.setFunctionContext(functionContext);
            xpathObj.setNamespaceContext(jaxenNSContext);
            results.add(xpathObj.evaluate(doc));
        }
        return results;
    }
}

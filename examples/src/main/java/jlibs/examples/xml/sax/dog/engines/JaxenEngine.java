/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.examples.xml.sax.dog.engines;

import jlibs.examples.xml.sax.dog.TestCase;
import jlibs.examples.xml.sax.dog.XPathEngine;
import jlibs.examples.xml.sax.dog.XPathInfo;
import jlibs.xml.ClarkName;
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
public class JaxenEngine extends XPathEngine {
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
        for(XPathInfo xpathInfo : testCase.xpaths){
            DOMXPath xpathObj = new DOMXPath(xpathInfo.xpath);
            xpathObj.setVariableContext(variableContext);
            xpathObj.setFunctionContext(functionContext);
            xpathObj.setNamespaceContext(jaxenNSContext);
            results.add(xpathObj.evaluate(doc));
        }
        return results;
    }
}

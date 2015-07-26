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
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.xpath.XPathEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

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
            xpe.getConfiguration().setVersionWarning(false);
            ((StandardErrorListener)xpe.getConfiguration().getErrorListener()).setRecoveryPolicy(Configuration.RECOVER_SILENTLY);
            xpe.getStaticContext().setBackwardsCompatibilityMode(true);
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

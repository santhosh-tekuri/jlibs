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

package jlibs.examples.xml.sax.dog;

import jlibs.examples.xml.sax.dog.engines.SaxonEngine;
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.XPathResults;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.sniff.DOMBuilder;
import jlibs.xml.sax.dog.sniff.Event;
import jlibs.xml.stream.STAXXMLReader;
import jlibs.xml.xpath.DefaultXPathVariableResolver;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFunctionResolver;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class TestCase{
    public static XMLReader saxXMLReader;
    public static XMLReader staxXMLReader;
    static{
        try{
            saxXMLReader = SAXUtil.newSAXFactory(true, false, false).newSAXParser().getXMLReader();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        staxXMLReader = new STAXXMLReader();
    }
    public static boolean useSTAX = false;
    public static boolean useXMLBuilder = false;
    public static boolean useInstantResults = false;
    public static XPathEngine domEngine =
//            new JDKEngine(new com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl());
//            new JDKEngine(new org.apache.xpath.jaxp.XPathFactoryImpl());
//            new JDKEngine(new net.sf.saxon.xpath.XPathFactoryImpl());
            new SaxonEngine();
//            new JaxenEngine();

    public String file;
    public List<XPathInfo> xpaths = new ArrayList<XPathInfo>();
    public DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
    public DefaultNamespaceContext resultNSContext = new DefaultNamespaceContext();
    public DefaultXPathVariableResolver variableResolver = new DefaultXPathVariableResolver();
    public XPathFunctionResolver functionResolver = new TestFunctionResolver();

    public List<Object> jdkResult = new ArrayList<Object>(xpaths.size());
    public List<Object> usingDOM() throws Exception{
        return jdkResult = domEngine.evaluate(this, file);
    }

    public List<Object> dogResult;
    public List<Object> usingXMLDog() throws Exception{
        InputSource source = new InputSource(file);
        final XMLDog dog = new XMLDog(nsContext, variableResolver, functionResolver);
        Expression expressions[] = new Expression[xpaths.size()];
        for(int i=0; i<xpaths.size(); i++){
            XPathInfo xpathInfo = xpaths.get(i);
            expressions[i] = xpathInfo.forEach==null ? dog.addXPath(xpathInfo.xpath) : dog.addForEach(xpathInfo.forEach, xpathInfo.xpath);
        }

        Event event = dog.createEvent();
        if(useXMLBuilder)
            event.setXMLBuilder(new DOMBuilder());

        dogResult = new ArrayList<Object>(xpaths.size());
        XMLReader reader = useSTAX ? staxXMLReader : saxXMLReader;
        if(useInstantResults){
            InstantXPathResults dogResults = new InstantXPathResults(dog.getDocumentXPathsCount());
            event.setListener(dogResults);
            dog.sniff(event, source, reader);
            for(Expression expr: expressions)
                dogResult.add(dogResults.getResult(expr));
        }else{
            XPathResults dogResults = new XPathResults(event);
            event.setListener(dogResults);
            dog.sniff(event, source, reader);
            for(Expression expr: expressions)
                dogResult.add(dogResults.getResult(expr));
        }
        resultNSContext = (DefaultNamespaceContext)event.getNamespaceContext();
        return dogResult;
    }

    public void translateDOMResult(int test){
        Object obj = jdkResult.get(test);
        List<?> result = domEngine.translate(obj, resultNSContext);
        jdkResult.set(test, result);

        xpaths.get(test).hasAttributes = has(result, NodeItem.ATTRIBUTE);
        xpaths.get(test).hasNamespaces = has(result, NodeItem.NAMESPACE);
    }

    private boolean has(List list, int nodeItemType){
        for(Object item: list){
            if(item instanceof NodeItem){
                NodeItem nodeItem = (NodeItem)item;
                if(nodeItem.type==nodeItemType)
                    return true;
            }else if(has((List)item, nodeItemType))
                return true;
        }
        return false;
    }

    private List<Integer> translated = new ArrayList<Integer>();
    public Object jdkResults(int i){
        if(!translated.contains(i) && xpaths.get(i).resultType.equals(XPathConstants.NODESET)){
            translateDOMResult(i);
            translated.add(i);
        }
        return jdkResult.get(i);
    }

    public Object dogResults(int i){
        return dogResult.get(i);
    }

    Comparator<NodeItem> attrComparator = new Comparator<NodeItem>(){
        @Override
        public int compare(NodeItem item1, NodeItem item2){
            return item1.location.compareTo(item2.location);
        }
    };

    Comparator<NodeItem> nsComparator = new Comparator<NodeItem>(){
        @Override
        public int compare(NodeItem item1, NodeItem item2){
            String location1 = item1.location.substring(item1.location.lastIndexOf('/'));
            String location2 = item2.location.substring(item2.location.lastIndexOf('/'));
            return location1.compareTo(location2);
        }
    };

    @SuppressWarnings({"unchecked"})
    public boolean passed(int i){
        Object jdkResults = jdkResults(i);
        Object dogResults = dogResults(i);

        if(jdkResults instanceof TreeSet)
            jdkResult.set(i, jdkResults = new ArrayList((TreeSet)jdkResults));
        if(dogResults instanceof TreeSet)
            dogResult.set(i, dogResults = new ArrayList((TreeSet)dogResults));

        if(xpaths.get(i).hasAttributes){
            sort((List)jdkResults, attrComparator);
            sort((List)dogResults, attrComparator);
        }else if(xpaths.get(i).hasNamespaces){
            sort((List)jdkResults, nsComparator);
            sort((List)dogResults, nsComparator);
        }

        if(jdkResults instanceof List)
            return equals((List<NodeItem>)jdkResults, (List<NodeItem>)dogResults);
        else
            return jdkResults.equals(dogResults);
    }

    @SuppressWarnings({"unchecked"})
    private void sort(List list, Comparator comparator){
        if(list.size()==0)
            return;
        if(list.get(0) instanceof List){
            for(Object item: list)
                sort((List)item, comparator);
        }else
            Collections.sort(list, comparator);
    }

    @SuppressWarnings({"unchecked"})
    private boolean equals(List<NodeItem> jdkList, List<NodeItem> dogList){
        if(jdkList.size()!=dogList.size())
            return false;

        Iterator jdkIter = jdkList.iterator();
        Iterator dogIter = dogList.iterator();
        while(jdkIter.hasNext()){
            Object jdkItem = jdkIter.next();
            Object dogItem = dogIter.next();

            if(jdkItem instanceof List){
                if(!equals((List)jdkItem, (List)dogItem))
                    return false;
            }else if(jdkItem instanceof NodeItem){
                NodeItem jdkNodeItem = (NodeItem)jdkItem;
                NodeItem dogNodeItem = (NodeItem)dogItem;
                String jdkLocation = jdkNodeItem.location;
                String dogLocation = dogNodeItem.location;
                if(dogNodeItem.type==NodeItem.NAMESPACE){
                    jdkLocation = jdkLocation.substring(jdkLocation.lastIndexOf('/'));
                    dogLocation = dogLocation.substring(dogLocation.lastIndexOf('/'));
                }
                if(!jdkLocation.equals(dogLocation))
                    return false;

                if(useXMLBuilder && !domEngine.equals(jdkNodeItem.xml, (Node)dogNodeItem.xml))
                    return false;
            }else{
                if(!jdkItem.equals(dogItem))
                    return false;
            }
        }

        return true;
    }

    /*-------------------------------------------------[ Printing ]---------------------------------------------------*/

    @SuppressWarnings({"unchecked"})
    public static int printResults(Object result){
        if(result instanceof Collection)
            return printResults((Collection)result);
        else{
            System.out.println(result);
            return -1;
        }
    }

    public static int printResults(Collection results){
        boolean first = true;
        for(Object item: results){
            if(first)
               first = false;
            else
                System.out.print(", ");
            System.out.print(item);
        }
        System.out.println();
        return results.size();
    }

    public void printResults(int i){
        System.out.println("            file : "+file);
        System.out.println("           xpath : "+xpaths.get(i));
        System.out.print("    "+TestCase.domEngine.getName()+" result : ");
        int count = printResults(jdkResults(i));
        if(count!=-1)
            System.out.println(" "+TestCase.domEngine.getName()+" hit-count : "+count);
        jdkResult.set(i, null);

        System.out.print("   XMLDog result : ");
        count = printResults(dogResults(i));
        if(count!=-1)
            System.out.println("XMLDog hit-count : "+count);
        dogResult.set(i, null);

        System.out.println("-------------------------------------------------");
    }
}
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
import jlibs.xml.sax.helpers.NamespaceSupportReader;
import jlibs.xml.sax.helpers.SAXHandler;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class XPathPerformanceTest{
    public List<List<String>> translateJDKResults(TestCase testCase){
        List<List<String>> results = new ArrayList<List<String>>();
        int test = 0;
        for(Object obj: testCase.jdkResult){
            testCase.hasAttributes.add(false);
            List<String> result = new ArrayList<String>();

            if(obj instanceof NodeList){
                NodeList nodeSet = (NodeList)obj;
                for(int i=0; i<nodeSet.getLength(); i++){
                    Node node = nodeSet.item(i);
                    if(node instanceof Attr){
                        result.add(node.getNodeValue());
                        testCase.hasAttributes.set(test, true);
                    }else if(node instanceof Element){
                        StringBuilder buff = new StringBuilder();
                        while(!(node instanceof Document)){
                            String prefix = testCase.nsContext.getPrefix(node.getNamespaceURI());
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
            results.add(result);
            test++;
        }
        testCase.jdkResult.clear();
        return results;
    }

    private List<TestCase> testCases = new ArrayList<TestCase>();
    private static HashMap<QName, List<String>> types = new HashMap<QName, List<String>>();
    static{
        List<String> list = new ArrayList<String>();
        list.add("name(");
        list.add("string(");
        list.add("normalize-space(");
        list.add("concat(");
        list.add("translate(");
        types.put(XPathConstants.STRING, list);

        list = new ArrayList<String>();
        list.add("number(");
        list.add("sum(");
        list.add("count(");
        list.add("string-length(");
        types.put(XPathConstants.NUMBER, list);

        list = new ArrayList<String>();
        list.add("boolean(");
        list.add("true(");
        list.add("false(");
        types.put(XPathConstants.BOOLEAN, list);
    }
    public void readTestCases(String configFile) throws Exception{
        new NamespaceSupportReader(true).parse(configFile, new SAXHandler(){
            TestCase testCase;
            CharArrayWriter contents = new CharArrayWriter();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
                if(localName.equals("testcase")){
                    testCases.add(testCase = new TestCase());
                    Enumeration<String> enumer = nsSupport.getPrefixes();
                    while(enumer.hasMoreElements()){
                        String prefix = enumer.nextElement();
                        testCase.nsContext.declarePrefix(prefix, nsSupport.getURI(prefix));
                    }
                    if(nsSupport.getURI("")!=null)
                        testCase.nsContext.declarePrefix("", nsSupport.getURI(""));
                }if(localName.equals("xpath")){
                    String type = attributes.getValue("type");
                    if(type!=null){
                        if(type.equals("nodeset"))
                            testCase.resultTypes.add(XPathConstants.NODESET);
                        else if(type.equals("string"))
                            testCase.resultTypes.add(XPathConstants.STRING);
                        else if(type.equals("number"))
                            testCase.resultTypes.add(XPathConstants.NUMBER);
                        else if(type.equals("boolean"))
                            testCase.resultTypes.add(XPathConstants.BOOLEAN);
                    }
                }
                contents.reset();
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException{
                contents.write(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException{
                if(localName.equals("file"))
                    testCase.file = contents.toString().trim();
                else if(localName.equals("xpath")){
                    String xpath = contents.toString().trim();
                    testCase.xpaths.add(xpath);

                    if(testCase.resultTypes.size()!=testCase.xpaths.size()){
                        for(Map.Entry<QName, List<String>> entry: types.entrySet()){
                            for(String str: entry.getValue()){
                                if(xpath.startsWith(str)){
                                    testCase.resultTypes.add(entry.getKey());
                                    return;
                                }
                            }
                        }
                        testCase.resultTypes.add(XPathConstants.NODESET);
                    }
                }

            }
        });
    }

    private void printResults(List<String> results){
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

    private long usingJDK() throws Exception{
        long time;
        System.out.print("XALAN:  ");
        time = System.nanoTime();
        for(TestCase testCase: testCases)
            testCase.jdkResult = testCase.usingJDK();
        long jdkTime = System.nanoTime() - time;
        System.out.println("Done");
        return jdkTime;
    }

    private long usingXMLDog() throws Exception{
        System.out.print("XMLDog: ");
        long time = System.nanoTime();
        for(TestCase testCase: testCases)
            testCase.dogResult = testCase.usingXMLDog();
        long dogTime = System.nanoTime() - time;
        System.out.println("Done");
        return dogTime;
    }

    public void run(String configFile) throws Exception{
        readTestCases(configFile);

        long dogTime = usingXMLDog();
        long jdkTime = usingJDK();
        System.out.println();

        int total= 0;
        int failed = 0;
        for(TestCase testCase: testCases){
            total += testCase.jdkResult.size();
            List<List<String>> jdkResults = translateJDKResults(testCase);

            PrintStream stream = System.out;

            for(int i=0; i<testCase.xpaths.size(); i++){
                List<String> jdkResult = jdkResults.get(i);
                List<String> dogResult = testCase.dogResult.get(i);

                if(testCase.hasAttributes.get(i)){
                    Collections.sort(jdkResult);
                    Collections.sort(dogResult);
                    System.out.println("sorting results...");
                }

                boolean matched = jdkResult.equals(dogResult);

                System.out.println(matched ? "SUCCESSFULL:" : "FAILED:");
                if(!matched)
                    failed++;

                stream.println("         xpath : "+testCase.xpaths.get(i));
                stream.print("    jdk result : ");
                printResults(jdkResult);
                stream.println("  jdk hitcount : "+jdkResult.size());
                stream.print("    dog result : ");
                printResults(dogResult);
                stream.println("  dog hitcount : "+dogResult.size());
                stream.flush();

                System.out.println("-------------------------------------------------");
                System.out.flush();
            }
        }
        System.out.format("testcases are executed: total=%d failed=%d %n", total, failed);

        System.out.format("       jdk time : %d nanoseconds/%.2f seconds %n", jdkTime, jdkTime*1E-09);
        System.out.format("       dog time : %d nanoseconds/%.2f seconds %n", dogTime, dogTime*1E-09);
        double faster = (1.0*Math.max(dogTime, jdkTime)/Math.min(dogTime, jdkTime));
        System.out.format("        WINNER : %s (%.2fx faster) %n", dogTime<=jdkTime ? "XMLDog" : "XALAN", faster);
        long diff = Math.abs(dogTime - jdkTime);
        System.out.format("    Difference : %d nanoseconds/%.2f seconds %n", diff, diff*1E-09);

        System.out.println("\n--------------------------------------------------------------\n\n");

        if(failed>0){
            for(int i=0; i<10; i++)
                System.out.println("FAILED FAILED FAILED FAILED FAILED");
        }
    }

    public static void main(String[] args) throws Exception{
        new XPathPerformanceTest().run(args.length==0 ? "xpaths.xml" : args[0]);
    }
}

class TestCase{
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
            XPath xpathObj = XPathFactory.newInstance().newXPath();
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
}
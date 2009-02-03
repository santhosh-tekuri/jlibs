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

import jlibs.xml.sax.helpers.NamespaceSupportReader;
import jlibs.xml.sax.helpers.SAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.io.CharArrayWriter;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class TestSuite{
    public List<TestCase> testCases = new ArrayList<TestCase>();
    public int total;

    public TestSuite() throws Exception{
        this("xpaths.xml");
    }
    
    public TestSuite(String configFile) throws Exception{
        readTestCases(configFile);
    }

    public long usingJDK() throws Exception{
        long time;
        System.out.print("XALAN:  ");
        time = System.nanoTime();
        for(TestCase testCase: testCases){
            testCase.jdkResult = testCase.usingJDK();
            System.out.print('.');
            testCase.jdkResult = null;
        }
        long jdkTime = System.nanoTime() - time;
        System.out.println("Done");
        return jdkTime;
    }

    public long usingXMLDog() throws Exception{
        System.out.print("XMLDog: ");
        long time = System.nanoTime();
        for(TestCase testCase: testCases){
            testCase.dogResult = testCase.usingXMLDog();
            System.out.print('.');            
            testCase.dogResult = null;
        }
        long dogTime = System.nanoTime() - time;
        System.out.println("Done");
        return dogTime;
    }

    /*-------------------------------------------------[ Loading ]---------------------------------------------------*/
    
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
                }else if(localName.equals("testcase"))
                    total += testCase.xpaths.size();
            }
        });
    }
}

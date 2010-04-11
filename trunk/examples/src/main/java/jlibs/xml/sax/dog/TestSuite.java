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

package jlibs.xml.sax.dog;

import jlibs.xml.sax.helpers.NamespaceSupportReader;
import jlibs.xml.sax.helpers.SAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.io.CharArrayWriter;
import java.io.File;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class TestSuite{
    public static final String DEFAULT_TEST_SUITE = "../resources/xpaths.xml";
    
    public List<TestCase> testCases = new ArrayList<TestCase>();
    public int total;

    public TestSuite() throws Exception{
        this(DEFAULT_TEST_SUITE);
    }
    
    public TestSuite(String configFile) throws Exception{
        readTestCases(configFile);
    }

    public long usingJDK() throws Exception{
        long time;
        System.out.format("%6s: ", TestCase.domEngine.getName());
        time = System.nanoTime();
        for(TestCase testCase: testCases){
            testCase.jdkResult = testCase.usingDOM();
            System.out.print('.');
            testCase.jdkResult = null;
        }
        long jdkTime = System.nanoTime() - time;
        System.out.println("Done");
        return jdkTime;
    }

    public long usingXMLDog() throws Exception{
        System.out.format("%6s: ", "XMLDog");
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
        list.add("local-name(");
        list.add("namespace-uri(");
        list.add("string(");
        list.add("substring(");
        list.add("substring-after(");
        list.add("substring-before(");
        list.add("normalize-space(");
        list.add("concat(");
        list.add("translate(");
        list.add("upper-case(");
        list.add("lower-case(");
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
        list.add("not(");
        list.add("contains(");
        list.add("starts-with(");
        list.add("ends-with(");
        types.put(XPathConstants.BOOLEAN, list);
    }

    private QName getResultType(String xpath){
        for(Map.Entry<QName, List<String>> entry: types.entrySet()){
            for(String str: entry.getValue()){
                if(xpath.startsWith(str))
                    return entry.getKey();
            }
        }
        return XPathConstants.NODESET;
    }

    public void readTestCases(final String configFile) throws Exception{
        new NamespaceSupportReader(true).parse(configFile, new SAXHandler(){
            boolean generateNewXPathsGlobal = true;
            boolean generateNewXPathsCurrent = true;
            TestCase testCase;
            CharArrayWriter contents = new CharArrayWriter();
            QName variableName;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
                if(localName.equals("xpaths")){
                    String value = attributes.getValue("generate");
                    if(value!=null)
                        generateNewXPathsGlobal = Boolean.valueOf(value);
                }else if(localName.equals("testcase")){
                    testCases.add(testCase = new TestCase());
                    Enumeration<String> enumer = nsSupport.getPrefixes();
                    while(enumer.hasMoreElements()){
                        String prefix = enumer.nextElement();
                        testCase.nsContext.declarePrefix(prefix, nsSupport.getURI(prefix));
                    }
                    if(nsSupport.getURI("")!=null)
                        testCase.nsContext.declarePrefix("", nsSupport.getURI(""));
                }else if(localName.equals("xpath")){
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

                    String value = attributes.getValue("generate");
                    generateNewXPathsCurrent = value!=null ? Boolean.valueOf(value) : generateNewXPathsGlobal;
                }else if(localName.equals("variable"))
                    variableName = testCase.nsContext.toQName(attributes.getValue("name"));
                contents.reset();
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException{
                contents.write(ch, start, length);
            }

            private ArrayList<String> files = new ArrayList<String>();

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException{
                if(localName.equals("file")){
                    File f = new File(contents.toString().trim());
                    if(!f.isAbsolute())
                        f = new File(new File(configFile).getParentFile(), f.getPath());
                    String file = f.getPath();
                    if(testCase.file==null){
                        testCase.file = file;
                    }else
                        files.add(file);
                }else if(localName.equals("xpath")){
                    String xpath = contents.toString().trim();
                    testCase.xpaths.add(xpath);
                    if(testCase.resultTypes.size()!=testCase.xpaths.size())
                        testCase.resultTypes.add(getResultType(xpath));

                    if(generateNewXPathsGlobal && generateNewXPathsCurrent){
                        QName resultType = testCase.resultTypes.get(testCase.resultTypes.size()-1);
                        if(resultType.equals(XPathConstants.NODESET)){
                            if(xpath.indexOf("namespace::")==-1){
                                testCase.xpaths.add("name("+xpath+")");
                                testCase.resultTypes.add(XPathConstants.STRING);
                                testCase.xpaths.add("local-name("+xpath+")");
                                testCase.resultTypes.add(XPathConstants.STRING);
                                testCase.xpaths.add("namespace-uri("+xpath+")");
                                testCase.resultTypes.add(XPathConstants.STRING);
                                testCase.xpaths.add("string("+xpath+")");
                                testCase.resultTypes.add(XPathConstants.STRING);

                                testCase.xpaths.add(xpath+"[1]");
                                testCase.resultTypes.add(XPathConstants.NODESET);
                                
                                testCase.xpaths.add(xpath+"[last()]");
                                testCase.resultTypes.add(XPathConstants.NODESET);

                                testCase.xpaths.add(xpath+"[position()>1 and position()<last()]");
                                testCase.resultTypes.add(XPathConstants.NODESET);
                            }
                            testCase.xpaths.add("count("+xpath+")");
                            testCase.resultTypes.add(XPathConstants.NUMBER);
                            testCase.xpaths.add("boolean("+xpath+")");
                            testCase.resultTypes.add(XPathConstants.BOOLEAN);
                        }
                    }
                }else if(localName.equals("testcase")){
                    total += testCase.xpaths.size();
                    for(String file: files){
                        TestCase t = new TestCase();
                        t.file = file;
                        t.nsContext = testCase.nsContext;
                        t.xpaths = testCase.xpaths;
                        t.resultTypes = testCase.resultTypes;
                        testCases.add(t);
                        total += t.xpaths.size();
                    }
                    files.clear();
                }else if(localName.equals("variable"))
                    testCase.variableResolver.defineVariable(variableName, contents.toString());
                contents.reset();
            }
        });
    }
}

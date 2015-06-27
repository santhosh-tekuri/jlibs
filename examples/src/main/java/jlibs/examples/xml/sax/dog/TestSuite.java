/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

import jlibs.core.net.URLUtil;
import jlibs.xml.sax.helpers.NamespaceSupportReader;
import jlibs.xml.sax.helpers.SAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.io.CharArrayWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class TestSuite{
    public static final String DEFAULT_TEST_SUITE = URLUtil.toSystemID(ClassLoader.getSystemClassLoader().getResource("."))+"/../resources/xpaths.xml";

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

    public void readTestCases(final String configFile) throws Exception{
        new NamespaceSupportReader(true).parse(configFile, new SAXHandler(){
            boolean generateNewXPathsGlobal = true;
            boolean generateNewXPathsCurrent = true;
            TestCase testCase;
            CharArrayWriter contents = new CharArrayWriter();
            QName variableName;
            XPathInfo xpathInfo;

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
                    xpathInfo = new XPathInfo();
                    xpathInfo.forEach = attributes.getValue("for-each");
                    String type = attributes.getValue("type");
                    if(type!=null){
                        if(type.equals("nodeset"))
                            xpathInfo.resultType = XPathConstants.NODESET;
                        else if(type.equals("string"))
                            xpathInfo.resultType = XPathConstants.STRING;
                        else if(type.equals("number"))
                            xpathInfo.resultType = XPathConstants.NUMBER;
                        else if(type.equals("boolean"))
                            xpathInfo.resultType = XPathConstants.BOOLEAN;
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
                    xpathInfo.xpath = xpath;
                    if(xpathInfo.resultType==null)
                        xpathInfo.guessResultType();
                    testCase.xpaths.add(xpathInfo);

                    if(generateNewXPathsGlobal && generateNewXPathsCurrent){
                        if(xpathInfo.resultType.equals(XPathConstants.NODESET)){
                            if(xpath.indexOf("namespace::")==-1){
                                testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, "name("+xpath+")", XPathConstants.STRING));
                                testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, "local-name("+xpath+")", XPathConstants.STRING));
                                testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, "namespace-uri("+xpath+")", XPathConstants.STRING));
                                testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, "string("+xpath+")", XPathConstants.STRING));

                                testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, xpath+"[1]", XPathConstants.NODESET));
                                testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, xpath+"[last()]", XPathConstants.NODESET));
                                testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, xpath+"[position()>1 and position()<last()]", XPathConstants.NODESET));
                            }
                            testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, "count("+xpath+")", XPathConstants.NUMBER));
                            testCase.xpaths.add(new XPathInfo(xpathInfo.forEach, "boolean("+xpath+")", XPathConstants.BOOLEAN));
                        }
                    }
                }else if(localName.equals("testcase")){
                    total += testCase.xpaths.size();
                    for(String file: files){
                        TestCase t = new TestCase();
                        t.file = file;
                        t.nsContext = testCase.nsContext;
                        t.xpaths = testCase.xpaths;
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

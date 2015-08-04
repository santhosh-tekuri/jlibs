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

package jlibs.xml.sax;

import jlibs.xml.xsl.TransformerUtil;
import org.custommonkey.xmlunit.XMLAssert;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static jlibs.xml.Namespaces.URI_SOAP12ENV;
import static jlibs.xml.Namespaces.URI_SOAPENV;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NamespaceReplacerTest{

    @Test
    public void withoutDefaultNamespace() throws Exception{
        String soap11Xml = getClass().getResource("/soap11.xml").toString();
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put(URI_SOAPENV, URI_SOAP12ENV);
        XMLReader reader = SAXUtil.newSAXParser(true, false, false).getXMLReader();
        NamespaceReplacer replacer = new NamespaceReplacer(reader, namespaces);
        Transformer transformer = TransformerUtil.newTransformer(null, true, 0, null);
        StringWriter writer = new StringWriter();
        transformer.transform(new SAXSource(replacer, new InputSource(soap11Xml)), new StreamResult(writer));
        String soap12Xml = getClass().getResource("/soap12.xml").toString();
        XMLAssert.assertXMLEqual(new InputSource(soap12Xml), new InputSource(new StringReader(writer.toString())));
    }

    @Test
    public void toDefaultNamespace() throws Exception{
        String soap11Xml = getClass().getResource("/soap11.xml").toString();
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("http://www.example.org/stock", "");
        XMLReader reader = SAXUtil.newSAXParser(true, false, false).getXMLReader();
        NamespaceReplacer replacer = new NamespaceReplacer(reader, namespaces);
        Transformer transformer = TransformerUtil.newTransformer(null, true, 0, null);
        StringWriter writer = new StringWriter();
        transformer.transform(new SAXSource(replacer, new InputSource(soap11Xml)), new StreamResult(writer));
        String soap12Xml = getClass().getResource("/soap11-default.xml").toString();
        XMLAssert.assertXMLEqual(new InputSource(soap12Xml), new InputSource(new StringReader(writer.toString())));
    }

    @Test
    public void fromDefaultNamespace() throws Exception{
        String soap11Xml = getClass().getResource("/soap11-default.xml").toString();
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("", "http://www.example.org/stock");
        XMLReader reader = SAXUtil.newSAXParser(true, false, false).getXMLReader();
        NamespaceReplacer replacer = new NamespaceReplacer(reader, namespaces);
        Transformer transformer = TransformerUtil.newTransformer(null, true, 0, null);
        StringWriter writer = new StringWriter();
        transformer.transform(new SAXSource(replacer, new InputSource(soap11Xml)), new StreamResult(writer));
        String soap12Xml = getClass().getResource("/soap11.xml").toString();
        XMLAssert.assertXMLEqual(new InputSource(soap12Xml), new InputSource(new StringReader(writer.toString())));
    }

    @Test
    public void toDefaultNamespaceWithEmptyPrefix() throws Exception{
        String input = "<test xmlns='http://test'/>";
        Map<String, String> namespaces = Collections.singletonMap("http://test", "");
        XMLReader reader = SAXUtil.newSAXParser(true, false, false).getXMLReader();
        NamespaceReplacer replacer = new NamespaceReplacer(reader, namespaces);
        Transformer transformer = TransformerUtil.newTransformer(null, true, 0, null);
        StringWriter writer = new StringWriter();
        transformer.transform(new SAXSource(replacer, new InputSource(new StringReader(input))), new StreamResult(writer));
        XMLAssert.assertXMLEqual("<test/>", writer.toString());
    }

    @Test
    public void toDefaultNamespaceWithEmptyPrefix2() throws Exception{
        String input = "<test xmlns='http://test'><child xmlns='http://empty'/></test>";
        Map<String, String> namespaces = Collections.singletonMap("http://test", "");
        XMLReader reader = SAXUtil.newSAXParser(true, false, false).getXMLReader();
        NamespaceReplacer replacer = new NamespaceReplacer(reader, namespaces);
        Transformer transformer = TransformerUtil.newTransformer(null, true, 0, null);
        StringWriter writer = new StringWriter();
        transformer.transform(new SAXSource(replacer, new InputSource(new StringReader(input))), new StreamResult(writer));
        XMLAssert.assertXMLEqual("<test><child xmlns='http://empty'/></test>", writer.toString());
    }
}

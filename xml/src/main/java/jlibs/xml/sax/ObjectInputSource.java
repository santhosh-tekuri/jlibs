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

package jlibs.xml.sax;

import jlibs.core.io.IOUtil;
import jlibs.xml.xsl.TransformerUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * An InputSource implementation which uses XMLDocument to convert
 * java object to XML
 * 
 * @author Santhosh Kumar T
 */
public abstract class ObjectInputSource<E> extends InputSource{
    protected final E input;

    public ObjectInputSource(E input){
        this.input = input;
    }

    public E getObject(){
        return input;
    }

    /*-------------------------------------------------[ bootstrap ]---------------------------------------------------*/

    void writeTo(SAXDelegate saxDelegate)throws SAXException{
        XMLDocument xml = new XMLDocument(saxDelegate);
        xml.startDocument();
        write(input, xml);
        xml.endDocument();
    }

    protected abstract void write(E input, XMLDocument xml) throws SAXException;

    /*-------------------------------------------------[ Serialization ]---------------------------------------------------*/

    private SAXSource createSource(){
        return new SAXSource(new XMLWriter(), this);
    }

    public void writeTo(Writer writer, boolean omitXMLDeclaration, int indentAmount) throws TransformerException{
        Transformer transformer = TransformerUtil.newTransformer(null, omitXMLDeclaration, indentAmount, null);
        transformer.transform(createSource(), new StreamResult(writer));
    }

    public void writeTo(OutputStream out, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerException{
        Transformer transformer = TransformerUtil.newTransformer(null, omitXMLDeclaration, indentAmount, encoding);
        transformer.transform(createSource(), new StreamResult(out));
    }

    public void writeTo(String systemID, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerException{
        Transformer transformer = TransformerUtil.newTransformer(null, omitXMLDeclaration, indentAmount, encoding);
        transformer.transform(createSource(), new StreamResult(systemID));
    }

    /*-------------------------------------------------[ Test ]---------------------------------------------------*/

    public static void main(String[] args) throws Exception{
        new ObjectInputSource<String>(null){
            @Override
            protected void write(String input, XMLDocument xml) throws SAXException{
                String google = "http://google.com";
                String yahoo = "http://yahoo.com";

                xml.addProcessingInstruction("san", "test='1.2'");

                xml.declarePrefix("google", google);
                xml.declarePrefix("yahoo", yahoo);
                xml.declarePrefix("http://msn.com");

                xml.startElement(google, "hello");
                xml.addAttribute("name", "value");
                xml.addElement("xyz", "helloworld");
                xml.addElement(google, "hai", "test");
                xml.addXML(new InputSource("xml/xsds/note.xsd"), true);
                xml.addComment("this is comment");
                xml.addCDATA("this is sample cdata");
            }
        }.writeTo(new OutputStreamWriter(System.out, IOUtil.UTF_8), false, 4);
    }
}

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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAXDelegate that prints sax event information before delgating.
 * This is useful for debugging purposes.
 * 
 * @author Santhosh Kumar T
 */
public class SAXDebugHandler extends SAXDelegate{
    public SAXDebugHandler(DefaultHandler delegate){
        super(delegate);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        System.out.print("<"+qName);
        for(int i=0; i<attributes.getLength(); i++)
            System.out.format(" %s='%s'", attributes.getQName(i), attributes.getValue(i));
        System.out.print(">");
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        System.out.print(new String(ch, start, length));
        super.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        System.out.print("</"+qName+">");
        super.endElement(uri, localName, qName);
    }
}

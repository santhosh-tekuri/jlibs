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

import jlibs.core.lang.Ansi;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Santhosh Kumar T
 */
public class AnsiHandler extends DefaultHandler{
    private static final Ansi TOKENS = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.CYAN, null);
    private static final Ansi ELEMENT  = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.YELLOW, null);
    private static final Ansi ATTR_NAME  = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.GREEN, null);
    private static final Ansi ATTR_VALUE  = new Ansi(Ansi.Attribute.NORMAL, Ansi.Color.MAGENTA, null);

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        TOKENS.out("<");
        ELEMENT.out(qName);

        for(int i=0; i<attributes.getLength(); i++){
            System.out.print(" ");
            ATTR_NAME.out(attributes.getQName(i));
            TOKENS.out("=\"");
            ATTR_VALUE.out(attributes.getValue(i));
            TOKENS.out("\"");
        }

        TOKENS.out(">");
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        TOKENS.out("</");
        ELEMENT.out(qName);
        TOKENS.out(">");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        System.out.print(new String(ch, start, length));
    }

    @Override
    public void endDocument() throws SAXException{
        System.out.println();
    }
}

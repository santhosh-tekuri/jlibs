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

package jlibs.xml.sax.dog.sniff;

import jlibs.xml.Namespaces;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * @author Santhosh Kumar T
 */
public final class SAXHandler extends DefaultHandler2{
    final Event event;
    final boolean langInterested;

    public SAXHandler(Event event, boolean langInterested){
        this.event = event;
        this.langInterested = langInterested;
    }

    public void startDocument() throws SAXException{
        nsSupport.startDocument();
        event.onStartDocument();
    }

    private final MyNamespaceSupport nsSupport = new MyNamespaceSupport();

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException{
        nsSupport.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException{
        nsSupport.startElement();

        Event event = this.event;
        event.onText();
        event.onStartElement(uri, localName, qName, langInterested ? attrs.getValue(Namespaces.URI_XML, "lang") : null);
        event.onNamespaces(nsSupport);
        event.onAttributes(attrs);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        event.appendText(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        nsSupport.endElement();

        event.onText();
        event.onEndElement();
    }

    @Override
    public void endDocument() throws SAXException{
        event.onEndDocument();
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException{
        event.onText();
        event.onPI(target, data);
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException{
        event.onText();
        event.onComment(ch, start, length);
    }
}

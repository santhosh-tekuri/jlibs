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

import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * This interface tells how to convert this object to xml
 * 
 * @author Santhosh Kumar T
 */
public interface SAXProducer{
    /**
     * Serialize this object to xml
     *
     * @param rootElement   can be null, in case it should use its default root element
     * @param xml           xml document into which serialization to be done
     */
    public void serializeTo(QName rootElement, XMLDocument xml) throws SAXException;
}

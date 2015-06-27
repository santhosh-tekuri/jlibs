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

package jlibs.xml.sax.dog.path.tests;

import jlibs.xml.ClarkName;
import jlibs.xml.sax.dog.NodeType;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class QName extends Constraint{
    public final String namespaceURI;
    public final String localName;

    public QName(int id, String namespaceURI, String localName){
        super(id);
        this.namespaceURI = namespaceURI;
        this.localName = localName;
    }

    @Override
    public boolean matches(Event event){
        switch(event.type()){
            case NodeType.ELEMENT:
            case NodeType.ATTRIBUTE:
                return namespaceURI.equals(event.namespaceURI()) && this.localName.equals(event.localName());
            case NodeType.NAMESPACE:
                return this.localName.equals(event.localName());
            default:
                return false;
        }
    }

    @Override
    public String toString(){
        return ClarkName.valueOf(namespaceURI, localName);
    }
}
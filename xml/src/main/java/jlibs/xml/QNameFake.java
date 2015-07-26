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

package jlibs.xml;

import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class QNameFake{
    public String namespaceURI;
    public String localPart;

    public QNameFake set(String namespaceURI, String localPart){
        this.namespaceURI = namespaceURI;
        this.localPart = localPart;
        return this;
    }

    @Override
    public int hashCode(){
        return namespaceURI.hashCode() ^ localPart.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof QName){
            QName that = (QName)obj;
            return this.namespaceURI.equals(that.getNamespaceURI()) && this.localPart.equals(that.getLocalPart());
        }else if(obj instanceof QNameFake){
            QNameFake that = (QNameFake)obj;
            return this.namespaceURI.equals(that.namespaceURI) && this.localPart.equals(that.localPart);
        }
        return false;
    }
}

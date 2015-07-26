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

package jlibs.xml.sax.binding;

import jlibs.xml.NamespaceMap;
import jlibs.xml.QNameFake;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public abstract class SAXContext<T>{
    public T object;

    public Map<QName, Object> temp;
    protected QNameFake qnameFake;

    public Map<QName, Object> temp(){
        if(temp==null)
            temp = new HashMap<QName, Object>();
        return temp;
    }

    /** If you want to use this, ensure that BindingHandler.setPopulateNamespaces(true) is done */
    protected NamespaceMap namespaceMap;
    public NamespaceMap namespaceMap(){
        return namespaceMap;
    }

    protected QName element;
    public QName element(){
        return element;
    }

    /*-------------------------------------------------[ Storing ]---------------------------------------------------*/

    public void put(QName qname, Object value){
        if(value!=null)
            temp().put(qname, value);
    }

    @SuppressWarnings({"unchecked"})
    public void add(QName qname, Object value){
        if(value!=null){
            temp();
            List<Object> list = (List<Object>)temp.get(qname);
            if(list==null)
                temp.put(qname, list=new ArrayList<Object>());
            list.add(value);
        }
    }

    /*-------------------------------------------------[ Storing Attributes ]---------------------------------------------------*/
    
    public void putAttributes(Attributes attributes){
        int attrCount = attributes.getLength();
        if(attrCount>0){
            temp();
            for(int i=0; i<attrCount; i++){
                QName qname = new QName(attributes.getURI(i), attributes.getLocalName(i));
                temp.put(qname, attributes.getValue(i));
            }
        }
    }

    public void putAttribute(Attributes attributes, QName qname){
        String value = attributes.getValue(qname.getNamespaceURI(), qname.getLocalPart());
        if(value!=null)
            put(qname, value);
    }

    public void addAttributes(Attributes attributes){
        int attrCount = attributes.getLength();
        if(attrCount>0){
            temp();
            for(int i=0; i<attrCount; i++){
                QName qname = new QName(attributes.getURI(i), attributes.getLocalName(i));
                add(qname, attributes.getValue(i));
            }
        }
    }

    public void addAttribute(Attributes attributes, QName qname){
        String value = attributes.getValue(qname.getNamespaceURI(), qname.getLocalPart());
        if(value!=null)
            add(qname, value);
    }

    /*-------------------------------------------------[ Retriving ]---------------------------------------------------*/
    
    public <X> X get(String namespaceURI, String localPart){
        return get(namespaceURI, localPart, (X)null);
    }

    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    public <X> X get(String namespaceURI, String localPart, X defaultValue){
        if(temp==null)
            return defaultValue;
        X value = (X)temp.get(qnameFake.set(namespaceURI, localPart));
        return value!=null ? value : defaultValue;
    }

    /*-------------------------------------------------[ Debuging ]---------------------------------------------------*/

    public abstract String xpath();
    public abstract Locator locator();

    public String toString(){
        return xpath() + " (line="+locator().getLineNumber()+", col="+locator().getColumnNumber()+')';
    }
}


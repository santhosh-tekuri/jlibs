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

package jlibs.xml.sax.async;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Santhosh Kumar T
 */
public class DTD{
    private AsyncXMLReader reader;
    public String root;
    public Map<String, Map<String, DTDAttribute>> attributes = new HashMap<String, Map<String, DTDAttribute>>();
    public Set<String> nonMixedElements = new HashSet<String>();
    public InputSource externalDTD;

    public DTD(AsyncXMLReader reader){
        this.reader = reader;
    }

    public void reset(){
        root = null;
        attributes.clear();
        nonMixedElements.clear();
        externalDTD = null;
    }

    public AttributeType attributeType(String element, String attribute){
        AttributeType type = AttributeType.CDATA;
        Map<String, DTDAttribute> attrList = attributes.get(element);
        if(attrList!=null){
            DTDAttribute dtdAttr = attrList.get(attribute);
            if(dtdAttr!=null)
                type = dtdAttr.type==AttributeType.ENUMERATION ? AttributeType.NMTOKEN : dtdAttr.type;
        }
        return type;
    }

    public void addMissingAttributes(String elemName, AttributesImpl attributes){
        Map<String, DTDAttribute> attList = this.attributes.get(elemName);
        if(attList==null)
            return;
        for(DTDAttribute dtdAttr: attList.values()){
            switch(dtdAttr.valueType){
                case DEFAULT:
                case FIXED:
                    if(attributes.getIndex(dtdAttr.name)==-1 && !dtdAttr.isNamespace()){
                        AttributeType type = dtdAttr.type==AttributeType.ENUMERATION ? AttributeType.NMTOKEN : dtdAttr.type;

                        String namespaceURI = "";
                        String localName = dtdAttr.name;
                        String qname = localName;
                        int colon = qname.indexOf(':');
                        if(colon!=-1){
                            localName = qname.substring(colon+1);
                            String prefix = qname.substring(0, colon);
                            if(prefix.length()>0)
                                namespaceURI = reader.getNamespaceURI(prefix);
                        }
                        attributes.addAttribute(namespaceURI, localName, qname, type.name(), dtdAttr.value);
                    }
            }
        }
    }
}

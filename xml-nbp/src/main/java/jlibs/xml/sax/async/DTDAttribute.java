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

package jlibs.xml.sax.async;

import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class DTDAttribute{
    public String element;
    
    public String name;
    public AttributeType type;
    public AttributeValueType valueType;
    public String value;
    public List<String> validValues;

    boolean isNamespace(){
        return name.startsWith("xmlns") && (name.length()==5 || name.charAt(5)==':');
    }
    
    void fire(DeclHandler handler) throws SAXException{
        if(handler!=null)
            handler.attributeDecl(element, name, type.toString(validValues), valueType.mode, value);
    }
}

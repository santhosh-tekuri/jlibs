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

package jlibs.xml.xpath;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class DefaultXPathVariableResolver implements XPathVariableResolver{
    private final Map<QName, Object> map;

    public DefaultXPathVariableResolver(){
        this(null);
    }

    public DefaultXPathVariableResolver(Map<QName, Object> map){
        if(map==null)
            map = new HashMap<QName, Object>();
        this.map = map;
    }

    public DefaultXPathVariableResolver defineVariable(QName variableName, Object value){
        map.put(variableName, value);
        return this;
    }
    
    public DefaultXPathVariableResolver defineVariable(String variableName, Object value){
        return defineVariable(new QName(variableName), value);
    }

    @Override
    public Object resolveVariable(QName variableName){
        return map.get(variableName);
    }
}

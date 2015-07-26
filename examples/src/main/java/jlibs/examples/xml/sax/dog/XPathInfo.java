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

package jlibs.examples.xml.sax.dog;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class XPathInfo{
    public String forEach;
    public String xpath;
    public QName resultType;
    public boolean hasAttributes;
    public boolean hasNamespaces;

    public XPathInfo(){
    }

    public XPathInfo(QName resultType){
        this.resultType = resultType;
    }
    
    public XPathInfo(String forEach, String xpath, QName resultType){
        this.forEach = forEach;
        this.xpath = xpath;
        this.resultType = resultType;
    }

    public void guessResultType(){
        for(Map.Entry<QName, List<String>> entry: types.entrySet()){
            for(String str: entry.getValue()){
                if(xpath.startsWith(str)){
                    resultType = entry.getKey();
                    return;
                }
            }
        }
        resultType = XPathConstants.NODESET;
    }

    @Override
    public String toString(){
        return forEach==null ? xpath : "#for-each "+forEach+" #eval "+xpath;
    }

    private static HashMap<QName, List<String>> types = new HashMap<QName, List<String>>();
    static{
        List<String> list = new ArrayList<String>();
        list.add("name(");
        list.add("local-name(");
        list.add("namespace-uri(");
        list.add("string(");
        list.add("substring(");
        list.add("substring-after(");
        list.add("substring-before(");
        list.add("normalize-space(");
        list.add("concat(");
        list.add("translate(");
        list.add("upper-case(");
        list.add("lower-case(");
        types.put(XPathConstants.STRING, list);

        list = new ArrayList<String>();
        list.add("number(");
        list.add("sum(");
        list.add("count(");
        list.add("string-length(");
        types.put(XPathConstants.NUMBER, list);

        list = new ArrayList<String>();
        list.add("boolean(");
        list.add("true(");
        list.add("false(");
        list.add("not(");
        list.add("contains(");
        list.add("starts-with(");
        list.add("ends-with(");
        types.put(XPathConstants.BOOLEAN, list);
    }
}

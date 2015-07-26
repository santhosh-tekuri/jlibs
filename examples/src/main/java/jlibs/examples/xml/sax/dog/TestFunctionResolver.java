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
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class TestFunctionResolver implements XPathFunctionResolver{
    @Override
    public XPathFunction resolveFunction(QName functionName, int arity) {
        if(functionName.equals(new QName("http://jlibs.googlecode.com", "reverse")))
            return REVERSE;
        else
            return null;
    }
    
    private static final XPathFunction REVERSE = new XPathFunction() {
        @Override
        public Object evaluate(List args) throws XPathFunctionException{
            char[] ch = ((String)args.get(0)).toCharArray();
            for(int i=0, j=ch.length-1; i<j; i++, j--){
                char temp = ch[i];
                ch[i] = ch[j];
                ch[j] = temp;
            }
            return new String(ch);
        }
    }; 
}

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

package jlibs.xml.sax;

import org.xml.sax.SAXException;

/**
 * ObjectInputSource implementation for Object implementing SAXProducer
 * 
 * @author Santhosh Kumar T
 */
public class SAXInputSource<E extends SAXProducer> extends ObjectInputSource<E>{
    public SAXInputSource(E obj){
        super(obj);
    }

    @Override
    protected void write(E input, XMLDocument xml) throws SAXException{
        xml.add(input);
    }
}

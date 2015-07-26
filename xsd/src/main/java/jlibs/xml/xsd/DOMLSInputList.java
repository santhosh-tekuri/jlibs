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

package jlibs.xml.xsd;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.xs.LSInputList;
import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings("unchecked")
public class DOMLSInputList extends ArrayList implements LSInputList{
    @Override
    public int getLength(){
        return size();
    }

    @Override
    public LSInput item(int i){
        return (LSInput)get(i);
    }

    public DOMInputImpl addSystemID(String systemID){
        DOMInputImpl input = new DOMInputImpl();
        input.setSystemId(systemID);
        add(input);
        return input;
    }

    public DOMInputImpl addStringData(String stringData, String systemID){
        DOMInputImpl input = new DOMInputImpl();
        input.setStringData(stringData);
        input.setSystemId(systemID);
        add(input);
        return input;
    }

    public DOMInputImpl addReader(Reader reader, String systemID){
        DOMInputImpl input = new DOMInputImpl();
        input.setCharacterStream(reader);
        input.setSystemId(systemID);
        add(input);
        return input;
    }

    public DOMInputImpl addStream(InputStream stream, String systemID, String encoding){
        DOMInputImpl input = new DOMInputImpl();
        input.setByteStream(stream);
        input.setSystemId(systemID);
        input.setEncoding(encoding);
        add(input);
        return input;
    }
}

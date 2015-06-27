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

package jlibs.xml.stream;

import org.xml.sax.ext.Locator2;

import javax.xml.stream.XMLStreamReader;

/**
 * SAX {@link Locator2} implementation for {@link XMLStreamReader}.
 * 
 * @author Santhosh Kumar T
 */
public class STAXLocator implements Locator2{
    private XMLStreamReader reader;

    public STAXLocator(XMLStreamReader reader){
        this.reader = reader;
    }

    @Override
    public String getPublicId(){
        return reader.getLocation().getPublicId();
    }

    @Override
    public String getSystemId(){
        return reader.getLocation().getSystemId();
    }

    @Override
    public int getLineNumber(){
        return reader.getLocation().getLineNumber();
    }

    @Override
    public int getColumnNumber(){
        return reader.getLocation().getColumnNumber();
    }

    @Override
    public String getXMLVersion() {
        return reader.getVersion();
    }

    @Override
    public String getEncoding() {
        return reader.getEncoding();
    }
}

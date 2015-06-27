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

import static jlibs.xml.sax.SAXFeatures.NAMESPACES;
import static jlibs.xml.sax.SAXFeatures.NAMESPACE_PREFIXES;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for XMLReader implementation
 *
 * @author Santhosh Kumar T
 */
public abstract class AbstractXMLReader extends BaseXMLReader{
    protected AbstractXMLReader(){
        supportedFeatures.add(SAXFeatures.NAMESPACES);
    }

    /*-------------------------------------------------[ Features ]---------------------------------------------------*/

    protected final Set<String> supportedFeatures = new HashSet<String>();
    private final Set<String> features = new HashSet<String>();

    protected boolean nsFeature;
    protected boolean nsPrefixesFeature;

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException{
        if(supportedFeatures.contains(name)){
            if(value)
                features.add(name);
            else
                features.remove(name);

            if(NAMESPACES.equals(name))
                nsFeature = value;
            else if(NAMESPACE_PREFIXES.equals(name))
                nsPrefixesFeature = value;
        }else
            throw new SAXNotRecognizedException(name);
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException{
        if(supportedFeatures.contains(name))
            return features.contains(name);
        else
            throw new SAXNotRecognizedException(name);
    }

    /*-------------------------------------------------[ Properties ]---------------------------------------------------*/

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException{
        if(!_setProperty(name, value))
            throw new SAXNotRecognizedException(name);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException{
        Object value = _getProperty(name);
        if(value!=null)
            return value;
        else
            throw new SAXNotRecognizedException(name);
    }
}

/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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

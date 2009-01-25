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

package jlibs.xml.sax.sniff.model.functions;

import jlibs.xml.sax.sniff.model.listeners.DerivedResults;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

/**
 * @author Santhosh Kumar T
 */
public class StringLength extends DerivedResults{
    @Override
    public QName resultType(){
        return XPathConstants.NUMBER;
    }

    public void joinResults(){
        String result = getResult(members.get(0));
        addResult(-1, String.valueOf((double)result.length()));
    }
}
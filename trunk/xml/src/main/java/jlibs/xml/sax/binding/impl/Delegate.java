/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.binding.impl;

import jlibs.xml.sax.binding.SAXContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
public class Delegate extends BindingCumRelation{
    private BindingCumRelation delegate;

    public Delegate(BindingCumRelation delegate){
        this.delegate = delegate;
    }

    @Override
    public void startRelation(int state, SAXContext parent, SAXContext current) throws SAXException{
        delegate.startRelation(state, parent, current);
    }

    @Override
    public void endRelation(int state, SAXContext parent, SAXContext current) throws SAXException{
        delegate.endRelation(state, parent, current);
    }

    @Override
    public void startElement(int state, SAXContext current, Attributes attributes) throws SAXException{
        delegate.startElement(state, current, attributes);
    }

    @Override
    public void text(int state, SAXContext current, String text) throws SAXException{
        delegate.text(state, current, text);
    }

    @Override
    public void endElement(int state, SAXContext current) throws SAXException{
        delegate.endElement(state, current);
    }
}

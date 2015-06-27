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
import org.xml.sax.SAXException;

/**
 * Establishes relation between parent and child objects
 *
 * @author Santhosh Kumar T
 */
public interface Relation{
    public void startRelation(int state, SAXContext parent, SAXContext current) throws SAXException;
    public void endRelation(int state, SAXContext parent, SAXContext current)throws SAXException;

    /*-------------------------------------------------[ DO NOTHING ]---------------------------------------------------*/

    static final Relation DO_NOTHING = new Relation(){
        @Override
        public void startRelation(int state, SAXContext parent, SAXContext current) throws SAXException{}
        @Override
        public void endRelation(int state, SAXContext parent, SAXContext current) throws SAXException{}
    };
}

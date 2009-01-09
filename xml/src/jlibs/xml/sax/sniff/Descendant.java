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

package jlibs.xml.sax.sniff;

import org.xml.sax.Attributes;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
abstract class Descendant extends Node{
    protected Descendant(Node parent){
        super(parent);
    }

    /*-------------------------------------------------[ Matching ]---------------------------------------------------*/

    @Override
    protected boolean matchesElement(String uri, String name){
        return true;
    }

    @Override
    protected List<Node> matchStartElement(String uri, String name){
        List<Node> list = matchChildren(uri, name);

        depth++;
        list.add(this);

        return list;
    }

    @Override
    protected void matchAttributes(Attributes attributes){
        _matchAttributes(attributes);
    }

    @Override
    protected void matchText(StringContent content){
        _matchText(content);
    }
}

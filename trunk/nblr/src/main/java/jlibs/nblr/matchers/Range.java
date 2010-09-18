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

package jlibs.nblr.matchers;

import jlibs.core.lang.StringUtil;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

/**
 * @author Santhosh Kumar T
 */
public final class Range extends Matcher{
    private char from, to;

    public Range(String chars){
        from = chars.charAt(0);
        to = chars.charAt(2);
    }

    @Override
    public boolean matches(char ch){
        return ch>=from && ch<=to;
    }

    @Override
    public String javaCode(){
        StringBuilder buff = new StringBuilder("ch>='");
        buff.append(StringUtil.toLiteral(from, false));
        buff.append("' && ch<='");
        buff.append(StringUtil.toLiteral(to, false));
        buff.append('\'');
        return buff.toString();
    }

    @Override
    public String toString(){
        return '['+encode(from)+'-'+encode(to)+']';
    }

    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/

    @Override
    protected void addBody(XMLDocument xml) throws SAXException{
        xml.addAttribute("from", ""+from);
        xml.addAttribute("to", ""+to);
    }
}

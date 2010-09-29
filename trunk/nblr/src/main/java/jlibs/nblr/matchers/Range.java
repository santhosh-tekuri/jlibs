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

import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class Range extends Matcher{
    private int from, to;

    public Range(String chars){
        from = chars.charAt(0);
        to = chars.charAt(2);
        if(from>to)
            throw new IllegalArgumentException("invalid range: "+this);
    }

    public Range(int from, int to){
        this.from = from;
        this.to = to;
    }

    @Override
    public String javaCode(String variable){
        return String.format("%s>=%s && %s<=%s", variable, toJava(from), variable, toJava(to));
    }

    public List<jlibs.core.util.Range> ranges(){
        return Collections.singletonList(new jlibs.core.util.Range(from, to)); 
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

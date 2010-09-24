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
import jlibs.core.util.Range;
import jlibs.xml.sax.XMLDocument;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class Any extends Matcher{
    private char chars[];

    public Any(String chars){
        if(chars!=null && chars.length()>0)
            this.chars = chars.toCharArray();
    }

    public Any(char ch){
        chars = new char[]{ ch };
    }

    public Any(){
        // match any char
    }

    @Override
    public boolean matches(char ch){
        if(chars==null)
            return true;
        for(char c: chars){
            if(c==ch)
                return true;
        }
        return false;
    }

    @Override
    public String javaCode(String variable){
        if(chars==null)
            return "true";

        StringBuilder buff = new StringBuilder();
        for(char ch: chars){
            if(buff.length()>0)
                buff.append(" || ");
            
            buff.append(variable).append("=='");
            buff.append(StringUtil.toLiteral(ch, false));
            buff.append('\'');
        }
        return buff.toString();
    }

    @Override
    public List<Range> ranges(){
        if(chars==null)
            return Collections.singletonList(new Range(Character.MIN_VALUE, Character.MAX_VALUE));
        else{
            List<Range> ranges = new ArrayList<Range>(chars.length);
            for(char ch: chars)
                ranges.add(new Range(ch, ch));
            return Range.union(ranges);
        }
    }

    @Override
    public String toString(){
        return '['+encode(chars)+']';
    }

    /*-------------------------------------------------[ SAXProducer ]---------------------------------------------------*/
    
    @Override
    protected void addBody(XMLDocument xml) throws SAXException{
        if(chars!=null)
            xml.addAttribute("chars", new String(chars));
    }
}
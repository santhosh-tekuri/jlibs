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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class Any extends Matcher{
    public static final Any NEW_LINE = new Any("\r\n");
    
    public final int chars[];

    public Any(String str){
        if(str!=null && str.length()>0)
            chars = StringUtil.toCodePoints(str);
        else
            chars = null;
    }

    public Any(char ch){
        chars = new int[]{ ch };
    }

    public Any(){
        chars = null;
    }

    public Any(int... codePoints){
        if(codePoints.length==0)
            chars = null;
        else
            chars = codePoints;
    }

    @Override
    public boolean canInline(){
        return super.canInline() || chars==null || chars.length==1;
    }

    @Override
    protected String __javaCode(String variable){
        if(chars==null)
            return variable+"!=-1";

        StringBuilder buff = new StringBuilder();
        for(int ch: chars){
            if(buff.length()>0)
                buff.append(" || ");
            buff.append(variable).append("==").append(toJava(ch));
        }
        return buff.toString();
    }

    @Override
    public List<Range> ranges(){
        if(chars==null)
            return Collections.singletonList(new Range(Character.MIN_VALUE, Character.MAX_VALUE));
        else{
            List<Range> ranges = new ArrayList<Range>(chars.length);
            for(int ch: chars)
                ranges.add(new Range(ch, ch));
            return Range.union(ranges);
        }
    }

    @Override
    public String toString(){
        return '['+encode(chars)+']';
    }
}
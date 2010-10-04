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

import jlibs.core.util.Range;

import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class Not extends Matcher{
    public final Matcher delegate;

    public Not(Matcher delegate){
        this.delegate = delegate;
    }

    @Override
    public boolean hasCustomJavaCode(){
        return super.hasCustomJavaCode() || delegate.hasCustomJavaCode();
    }

    @Override
    protected String __javaCode(String variable){
        return "!("+delegate._javaCode(variable)+")";
    }

    public static int minValue = Character.MIN_VALUE;
    @Override
    public List<Range> ranges(){
        return Range.minus(Collections.singletonList(new Range(minValue, Character.MAX_VALUE)), delegate.ranges());
    }

    @Override
    public String toString(){
        String msg = delegate._toString();
        msg = msg.substring(1, msg.length()-1);
        return "[^"+msg+"]";
    }
}

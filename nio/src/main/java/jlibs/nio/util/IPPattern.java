/*
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

package jlibs.nio.util;

import java.net.InetAddress;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class IPPattern{
    public final String pattern;
    protected byte[] mask;
    protected byte[] result;
    protected IPPattern(String pattern){
        this.pattern = pattern;
    }

    public boolean matches(InetAddress address){
        byte[] bytes = address.getAddress();
        if(bytes==null || bytes.length!=mask.length)
            return false;
        for(int i=0; i<mask.length; ++i){
            if((bytes[i]&mask[i])!=result[i])
                return false;
        }
        return true;
    }

    public static int intBitMask(int low, int high){
        assert low >= 0;
        assert low <= high;
        assert high < 32;
        return (high == 31 ? 0 : (1 << high + 1)) - (1 << low);
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()+"["+pattern+"]";
    }
}

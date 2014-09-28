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

/**
 * Pattern can be based on:<ul></ul>
 * <li>Wildcards: 208.130.*.*</li>
 * <li>CIDR Notation: 208.130.28.0/22</li>
 * </ul>
 *
 * @author Santhosh Kumar Tekuri
 */
public class IPV4Pattern extends IPPattern{
    public IPV4Pattern(String pattern){
        super(pattern);
        result = new byte[4];
        mask = new byte[4];

        int slash = pattern.indexOf('/');
        if(slash==-1){
            String[] parts = pattern.split("\\.");
            if(parts.length!=4)
                throw new IllegalArgumentException();
            for(int i=0; i<4; ++i) {
                if(!parts[i].equals("*")){
                    result[i] = Byte.parseByte(parts[i]);
                    mask[i] = (byte)(0xFF);
                }
            }
        }else{
            int subnet = Integer.parseInt(pattern.substring(slash+1));
            if(subnet<0 || subnet>32)
                throw new IllegalArgumentException();
            for(int i=0; i<4; ++i){
                if(subnet>8){
                    mask[i] = (byte)(0xFF);
                    subnet -= 8;
                }else if(subnet!=0) {
                    mask[i] = (byte)(intBitMask(8-subnet, 7) & 0xFF);
                    subnet = 0;
                }else
                    break;
            }

            String[] parts = pattern.substring(0, slash).split("\\.");
            if(parts.length!=4)
                throw new IllegalArgumentException();
            for(int i=0; i<4; ++i)
                result[i] = (byte)Byte.parseByte(parts[i]);
        }
    }
}

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
 * Pattern can be based on:<ul>
 * <li>Wildcards: 2001:0db8:0123:4567:89ab:cdef:*:*</li>
 * <li>CIDR Notation: Wildcards: 2001:0db8:0123:4567:89ab:cdef::/32</li>
 * </ul>
 *
 * @author Santhosh Kumar Tekuri
 */
public class IPV6Pattern extends IPPattern{
    public IPV6Pattern(String pattern){
        super(pattern);
        result = new byte[16];
        mask = new byte[16];

        int slash = pattern.indexOf('/');
        if(slash==-1){
            String[] parts = pattern.split("\\:");
            if(parts.length!=8)
                throw new IllegalArgumentException();
            for(int i=0; i<8; ++i) {
                if(!parts[i].equals("*")){
                    int val = parts[i].isEmpty() ? 0 : Integer.parseInt(parts[i], 16);
                    result[i*2] = (byte)(val >> 8);
                    result[i*2+1] = (byte)(val & 0xFF);
                    mask[i*2] = (byte)(0xFF);
                    mask[i*2+1] = (byte)(0xFF);
                }
            }
        }else{
            int subnet = Integer.parseInt(pattern.substring(slash+1));
            if(subnet<0 || subnet>128)
                throw new IllegalArgumentException();
            for(int i=0; i<16; ++i){
                if(subnet>8){
                    mask[i] = (byte)(0xFF);
                    subnet -= 8;
                }else if(subnet!=0) {
                    mask[i] = (byte)(intBitMask(8-subnet, 7) & 0xFF);
                    subnet = 0;
                }else
                    break;
            }

            String[] parts = pattern.substring(0, slash).split("\\:");
            for(int i=0; i<8; ++i){
                int val = parts[i].isEmpty() ? 0 : Integer.parseInt(parts[i], 16);
                result[i*2] = (byte) (val >> 8);
                result[i*2+1] = (byte) (val & 0xFF);
            }
        }
    }
}

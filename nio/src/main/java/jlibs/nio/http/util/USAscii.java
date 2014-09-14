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

package jlibs.nio.http.util;

import java.nio.ByteBuffer;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class USAscii{
    public static final boolean DIGIT[] = new boolean[128];
    static{
        for(int i='0'; i<='9'; i++)
            DIGIT[i] = true;
    }

    public static final boolean CTL[] = new boolean[128];
    static{
        for(int i=0; i<=31; i++)
            CTL[i] = true;
        CTL[127] = true;
    }

    public static final byte CR = 13;
    public static final byte LF = 10;
    public static final byte SP = 32;
    public static final byte HT = 9;
    public static final byte QUOTE = 34;
    public static final byte COLON = ':';
    public static final byte DASH = '-';

    public static final boolean WS[] = new boolean[128];
    static{
        WS[SP] = true;
        WS[HT] = true;
    }

    public static final boolean TOKEN[] = new boolean[128];
    static{
        for(int i=0; i<128; i++)
            TOKEN[i] = !CTL[i];
        String separators = "()<>@,;:\\\"/[]?={} \t";
        for(char ch: separators.toCharArray())
            TOKEN[ch] = false;
    }

    public static final boolean HEX[] = new boolean[128];
    static{
        for(int i='0'; i<='9'; i++)
            HEX[i] = true;
        for(int i='A'; i<='F'; i++)
            HEX[i] = true;
        for(int i='a'; i<='f'; i++)
            HEX[i] = true;
    }

    public static int toUpperCase(char ascii){
        return ascii>='a' && ascii<='z' ? (ascii & 0xDF) : ascii;
    }

    public static int toUpperCase(byte ascii){
        return ascii>='a' && ascii<='z' ? (ascii & 0xDF) : ascii;
    }

    public static byte[] toBytes(String str){
        int len = str.length();
        byte bytes[] = new byte[len];
        for(int i=0; i<len; i++)
            bytes[i] = (byte)str.charAt(i);
        return bytes;
    }

    public static void append(ByteBuffer buffer, String str){
        int len = str.length();
        for(int i=0; i<len; i++)
            buffer.put((byte)str.charAt(i));
    }

    public static int caseInsensitiveHashCode(CharSequence seq){
        int hash = 17;
        int len = seq.length();
        for(int i=0; i<len; ++i){
            int ch = seq.charAt(i);
            if(ch>='a' && ch<='z')
                ch = ch & 0xDF;
            hash = (hash<<4) + hash + ch;
        }
        return hash;
    }

    public static boolean equalIgnoreCase(CharSequence seq1, CharSequence seq2){
        if(seq1==seq2)
            return true;
        if(seq1==null || seq2==null || seq1.length()!=seq2.length())
            return false;
        int len = seq1.length();
        for(int i=0; i<len; i++){
            int ch1 = seq1.charAt(i);
            if(ch1>='a' && ch1<='z')
                ch1 = ch1 & 0xDF;

            int ch2 = seq2.charAt(i);
            if(ch2>='a' && ch2<='z')
                ch2 = ch2 & 0xDF;

            if(ch1!=ch2)
                return false;
        }
        return true;
    }
}

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

package jlibs.core.util.i18n;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * @author Santhosh Kumar T
 */
public final class PropertiesUtil{
    public static void writeComments(BufferedWriter bw,  String comments) throws IOException{
        bw.write("#");
        int len = comments.length();
        int current = 0;
        int last = 0;
        char[] uu = new char[6];
        uu[0] = '\\';
        uu[1] = 'u';
        while(current<len){
            char c = comments.charAt(current);
	        if(c>'\u00ff' || c=='\n' || c=='\r'){
	            if(last != current)
                    bw.write(comments.substring(last, current));
                if(c>'\u00ff'){
                    uu[2] = toHex((c >> 12) & 0xf);
                    uu[3] = toHex((c >>  8) & 0xf);
                    uu[4] = toHex((c >>  4) & 0xf);
                    uu[5] = toHex( c        & 0xf);
                    bw.write(new String(uu));
                }else{
                    bw.newLine();
                    if(c=='\r' && current!=len-1 && comments.charAt(current+1)=='\n'){
                        current++;
                    }
                    if(current==len-1 || (comments.charAt(current + 1)!='#' && comments.charAt(current+1)!='!'))
                        bw.write("#");
                }
                last = current + 1;
	        }
            current++;
	    }
        if(last!=current)
            bw.write(comments.substring(last, current));
        bw.newLine();
    }

    public static void writeProperty(BufferedWriter bw, String key, String value) throws IOException{
        key = saveConvert(key, true, false);
        value = saveConvert(value, false, false);
        bw.write(key + "=" + value);
        bw.newLine();
    }
    
    private static String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode){
        int len = theString.length();
        int bufLen = len * 2;
        if(bufLen<0)
            bufLen = Integer.MAX_VALUE;
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++){
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if (aChar>61 && aChar<127){
                if(aChar=='\\'){
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar){
                case ' ':
                    if (x == 0 || escapeSpace)
                    outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\'); outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\'); outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\'); outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\'); outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if((aChar<0x0020 || aChar>0x007e) & escapeUnicode){
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    }else
                        outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    private static char toHex(int nibble){
    	return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit ={
	    '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    public static NavigableSet<Integer> findArgs(String pattern){
        NavigableSet<Integer> args = new TreeSet<Integer>();
        StringBuffer[] segments = new StringBuffer[4];
        for(int i=0; i<segments.length; ++i)
            segments[i] = new StringBuffer();
        int part = 0;
        boolean inQuote = false;
        int braceStack = 0;
        for(int i=0; i<pattern.length(); ++i){
            char ch = pattern.charAt(i);
            if(part==0){
                if(ch=='\''){
                    if(i+1<pattern.length() && pattern.charAt(i+1)=='\''){
                        segments[part].append(ch);  // handle doubles
                        ++i;
                    }else
                        inQuote = !inQuote;
                }else if(ch=='{'&&!inQuote)
                    part = 1;
                else
                    segments[part].append(ch);
            }else if(inQuote){              // just copy quotes in parts
                segments[part].append(ch);
                if(ch=='\'')
                    inQuote = false;
            }else{
                switch(ch){
                    case ',':
                        if(part<3)
                            part += 1;
                        else
                            segments[part].append(ch);
                        break;
                    case '{':
                        ++braceStack;
                        segments[part].append(ch);
                        break;
                    case '}':
                        if(braceStack==0){
                            part = 0;
                            args.add(Integer.parseInt(segments[1].toString()));
                            segments[1].setLength(0);   // throw away other segments
                            segments[2].setLength(0);
                            segments[3].setLength(0);
                        }else{
                            --braceStack;
                            segments[part].append(ch);
                        }
                        break;
                    case '\'':
                        inQuote = true;
                        // fall through, so we keep quotes in other parts
                    default:
                        segments[part].append(ch);
                        break;
                }
            }
        }
        if(braceStack==0 && part!=0)
            throw new IllegalArgumentException("Unmatched braces in the pattern.");
        return args;
    }
}

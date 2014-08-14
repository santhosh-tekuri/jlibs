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

package jlibs.nio.http.msg.spec;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Parser{
    private final boolean foldable;
    public Parser(boolean foldable, String string){
        this.foldable = foldable;
        reset(string);
    }

    private String string;
    public String string(){
        return string;
    }

    private int index;
    public int index(){
        return index;
    }

    public boolean isEmpty(){
        return index>=string.length();
    }

    public void reset(String string){
        this.string = string;
        index = 0;
        skipWhitespace();
    }

    public void skip(){
        ++index;
        skipWhitespace();
    }

    private void skipWhitespace(){
        while(index<string.length() && Character.isWhitespace(string.charAt(index)))
            ++index;
    }

    /*-------------------------------------------------[ Index-Of ]---------------------------------------------------*/

    public int indexOf(char... candidates){
        int i = index;
        while(i<string.length()){
            char ch = string.charAt(i);
            if(ch=='"')
                i = matchingQuote(i);
            else{
                for(char candidate: candidates){
                    if(ch==candidate)
                        return i;
                }
            }
            ++i;
        }
        return -1;
    }

    /*-------------------------------------------------[ User-Methods ]---------------------------------------------------*/

    private String value(int to){
        while(to>index && Character.isWhitespace(string.charAt(to-1)))
            --to;

        if(string.charAt(index)=='"' && matchingQuote(index)==to-1)
            return quotedString();
        else
            return string.substring(index, to);
    }

    public String value(char... delimiters){
        skipWhitespace();
        int delimiter = indexOf(delimiters);
        if(delimiter==-1)
            delimiter = string.length();
        String value = value(delimiter);
        index = delimiter;
        return value;
    }

    private static final char COMMA[] = { ',' };
    private static final char SEMICOLON[] = { ';' };
    private static final char EQUAL_SEMICOLON[] = { '=', ';' };
    private static final char EQUAL_SEMICOLON_COMMA[] = { '=', ';', ',' };
    private static final char SEMICOLON_COMMA[] = { ';', ',' };

    public String value(){
        if(foldable){
            if(isEmpty())
                return null;
            if(string.charAt(index)==',')
                skip();
            return value(COMMA);
        }else
            return value(string.length());
    }

    public String lvalue(){
        if(isEmpty() || string.charAt(index)==',')
            return null;
        else{
            if(string.charAt(index)==';')
                skip();
            return value(foldable ? EQUAL_SEMICOLON_COMMA : EQUAL_SEMICOLON);
        }
    }

    public String rvalue(){
        if(isEmpty() || string.charAt(index)!='=')
            return null;
        else{
            skip();
            return value(foldable ? SEMICOLON_COMMA : SEMICOLON);
        }
    }

    public void skipPairs(){
        while(lvalue()!=null)
            rvalue();
    }

    /*-------------------------------------------------[ Helper ]---------------------------------------------------*/

    private int matchingQuote(int i){
        if(string.charAt(i)!='"')
            throw new UnsupportedOperationException();

        ++i;
        boolean escape = false;
        while(i<string.length()){
            char ch = string.charAt(i);
            if(escape)
                escape = false;
            else if(ch=='\\')
                escape = true;
            else if(ch=='"')
                return i;
            ++i;
        }
        throw new IllegalArgumentException("unterminated quoted-string");
    }

    public String quotedString(){
        if(string.charAt(index)!='"')
            throw new UnsupportedOperationException();

        StringBuilder buffer = null;
        ++index;
        int begin = index;
        boolean escape = false;
        while(index<string.length()){
            char ch = string.charAt(index);
            if(escape){
                buffer.append(ch);
                escape = false;
            }else if(ch=='\\'){
                escape = true;
                if(buffer==null)
                    buffer = new StringBuilder().append(string.substring(begin, index));
            }else if(ch=='"'){
                ++index;
                return buffer==null ? string.substring(begin, index-1) : buffer.toString();
            }else if(buffer!=null)
                buffer.append(ch);
            ++index;
        }
        throw new IllegalArgumentException("unterminated quoted-string");
    }

    public static StringBuilder appendQuotedValue(StringBuilder buffer, String name, String value){
        buffer.append(name);
        if(value!=null){
            buffer.append('=');
            appendQuotedString(buffer, value);
        }
        return buffer;
    }

    public static StringBuilder appendQuotedString(StringBuilder buffer, String value){
        buffer.append('"');
        for(int i=0; i<value.length(); i++){
            char ch = value.charAt(i);
            if(ch=='\\' || ch=='"')
                buffer.append('\\');
            buffer.append(ch);
        }
        buffer.append('"');
        return buffer;
    }

    public static StringBuilder appendValue(StringBuilder buffer, String name, String value){
        buffer.append(name);
        if(value!=null){
            buffer.append('=');
            appendQuotedIfWhitespace(buffer, value);
        }
        return buffer;
    }

    public static StringBuilder appendQuotedIfWhitespace(StringBuilder buffer, String value){
        boolean hasWhitespace = false;
        for(int i=0; i<value.length(); i++){
            if(Character.isWhitespace(value.charAt(i))){
                hasWhitespace = true;
                break;
            }
        }
        if(hasWhitespace)
            appendQuotedString(buffer, value);
        else
            buffer.append(value);
        return buffer;
    }
}

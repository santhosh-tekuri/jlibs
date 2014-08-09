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

package jlibs.nio.http.msg.spec.values;

/**
 * @author Santhosh Kumar Tekuri
 */
public class HeaderValueParser{
    private String line;
    private int index;
    private boolean stopAtComma;

    public HeaderValueParser(String line, boolean multipleElements){
        this.line = line;
        stopAtComma = multipleElements;
        skipWhitespace();
    }

    public String line(){
        return line;
    }

    public boolean hasNext(){
        return index<line.length();
    }

    private String name;
    public String name(){
        return name;
    }

    private String value;
    public String value(){
        return value;
    }

    public boolean nextElement(){
        if(index==0 && hasNext())
            return nextPair();
        else if(index<line.length() && line.charAt(index)==','){
            ++index;
            if(!hasNext())
                throw new IllegalArgumentException(line);
            return nextPair();
        }else
            return false;
    }

    public boolean nextParam(){
        if(index<line.length() && line.charAt(index)==';'){
            ++index;
            if(!hasNext())
                throw new IllegalArgumentException(line);
            return nextPair();
        }else
            return false;
    }

    private boolean nextPair(){
        name = null;
        value = null;

        int from = index;
        while(index<=line.length()){
            if(index==line.length()){
                name = line.substring(from, index).trim();
                return true;
            }else{
                char ch = line.charAt(index);
                if(ch=='='){
                    name = line.substring(from, index).trim();
                    ++index;
                    break;
                }else if((ch==',' && stopAtComma) || ch==';'){
                    name = line.substring(from, index).trim();
                    return true;
                }else
                    ++index;
            }
        }
        assert name!=null;

        skipWhitespace();
        if(index==line.length())
            return true;
        else if(line.charAt(index)=='"'){
            ++index;
            StringBuilder buffer = new StringBuilder();
            boolean escaped = false;
            while(index<line.length()){
                char ch = line.charAt(index);
                if(escaped){
                    buffer.append(ch);
                    escaped = false;
                }else if(ch=='\\')
                    escaped = true;
                else if(ch=='"'){
                    value = buffer.toString();
                    ++index;
                    skipWhitespace();
                    return true;
                }else
                    buffer.append(ch);
                ++index;
            }
            throw new IllegalArgumentException("unterminated quoted-string");
        }else{
            from = index;
            while(index<=line.length()){
                if(index==line.length()){
                    value = line.substring(from, index).trim();
                    return true;
                }else{
                    char ch = line.charAt(index);
                    if((ch==',' && stopAtComma) || ch==';'){
                        value = line.substring(from, index).trim();
                        return true;
                    }
                }
                index++;
            }
            throw new AssertionError();
        }
    }

    private void skipWhitespace(){
        while(index<line.length() && Character.isWhitespace(line.charAt(index)))
            ++index;
    }
}

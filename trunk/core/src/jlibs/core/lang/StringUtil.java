/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.lang;

import jlibs.core.graph.Filter;
import jlibs.core.util.regex.TemplateMatcher;

import java.util.Collections;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class StringUtil{
    /**
     * returns true if <code>str</code> is null or
     * its length is zero
     */
    public static boolean isEmpty(CharSequence str){
        return str==null || str.length()==0;
    }

    /**
     * returns true if <code>str</code> is null or
     * it contains only whitespaces.
     * <p>
     * {@link Character#isWhitespace(char)} is used
     * to test for whitespace
     */
    public static boolean isWhitespace(CharSequence str){
        if(str!=null){
            for(int i=0; i<str.length(); i++){
                if(!Character.isWhitespace(str.charAt(i)))
                    return false;
            }
        }
        return true;
    }

    public static String[] getTokens(String str, String delim, boolean trim){
        StringTokenizer stok = new StringTokenizer(str, delim);
        String tokens[] = new String[stok.countTokens()];
        for(int i=0; i<tokens.length; i++){
            tokens[i] = stok.nextToken();
            if(trim)
                tokens[i] = tokens[i].trim();
        }
        return tokens;
    }

    public static String suggest(Filter<String> filter, String pattern, boolean tryEmptyVar){
        TemplateMatcher matcher = new TemplateMatcher("${", "}");

        if(tryEmptyVar){
            String value = matcher.replace(pattern, Collections.singletonMap("var", ""));
            if(filter.select(value))
                return value;
        }

        int i = tryEmptyVar ? 2 :1;
        while(true){
            String value = matcher.replace(pattern, Collections.singletonMap("var", String.valueOf(i)));
            if(filter.select(value))
                return value;
            i++;
        }
    }
}

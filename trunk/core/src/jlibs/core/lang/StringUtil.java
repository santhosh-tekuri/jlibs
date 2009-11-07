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

    /**
     * Splits given string into tokens with delimiters specified.
     * It uses StringTokenizer for tokenizing.
     *
     * @param str       string to be tokenized
     * @param delim     delimiters used for tokenizing
     * @param trim      trim the tokens
     *
     * @return non-null token array
     */
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

    /**
     * the pattern specified must have variable part ${i}
     * example: test${i}.txt
     *
     * it will find a string using pattern, which is accepted by the specified filter.
     * if tryEmptyVar is true, it searches in order:
     *      test.txt, test2.txt, test3.txt and so on
     * if tryEmptyVar is false, it searches in order:
     *      test1.txt, test2.txt, test3.txt and so on
     *
     * @see jlibs.core.io.FileUtil#findFreeFile(java.io.File dir, String pattern, boolean tryEmptyVar)
     */
    public static String suggest(Filter<String> filter, String pattern, boolean tryEmptyVar){
        if(pattern.indexOf("${i}")==-1)
            throw new IllegalArgumentException("pattern must have ${i}");

        TemplateMatcher matcher = new TemplateMatcher("${", "}");

        if(tryEmptyVar){
            String value = matcher.replace(pattern, Collections.singletonMap("i", ""));
            if(filter.select(value))
                return value;
        }

        int i = tryEmptyVar ? 2 : 1;
        while(true){
            String value = matcher.replace(pattern, Collections.singletonMap("i", String.valueOf(i)));
            if(filter.select(value))
                return value;
            i++;
        }
    }
}

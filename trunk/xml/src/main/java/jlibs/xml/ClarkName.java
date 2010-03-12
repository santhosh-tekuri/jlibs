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

package jlibs.xml;

import jlibs.core.lang.StringUtil;

import java.util.Stack;
import java.util.StringTokenizer;

/**
 * This class contains utilties to work with ClarkName.
 * <p>
 * ClarkName is string representation of {@link javax.xml.namespace.QName} and
 * <a href="http://jclark.com/xml/xmlns.htm">defined</a> by James Clark.
 *
 * <pre class="prettyprint">
 * System.out.println(ClarkName.valueOf("htpp://namespace1", "elem1")); // prints "{http://namespace1}elem1"
 * System.out.println(ClarkName.valueOf("", "elem2")); // prints "elem2"
 * </pre>
 * The {@code String} returned by {@link #valueOf(String, String)} is compatible with {@link javax.xml.namespace.QName#toString()}.
 * <p>
 * To get {@code namespace} and {@code localPart} back from {@code clarkName}, use {@link #split(String)}:
 * <pre class="prettyprint">
 * String clarkName = ClarkName.valueOf("htpp://namespace1", "elem1");
 * String str[] = ClarkName.split(clarkName);
 * System.out.println(str[0]); // prints "http://namespace1"
 * System.out.println(str[2]); // prints "elem1"
 * </pre>
 * <b>ClarkPath:</b>
 * <p>
 * Suppose you want to save location of particular element in xpath form.<br>
 * For example:
 * <pre class="prettyprint">
 * ns1:elem1/ns2:elem2/ns2:elem3
 * where
 * ns1="http://namespace1"
 * ns2="http://namespace2"
 * ns3="http://namespace3"
 * </pre>
 * You can convert into raw xpath by inlining namespaces into xpath:
 * <pre class="prettyprint">
 * {http://namespace1}elem1/{http://namespace2}elem2/{http://namespace1}elem3
 * </pre>
 * This form will be easier to save to some file and restore it back.<br>
 * {@code ClarkName} provides handy method to split such paths.
 * <pre class="prettyprint">
 * String clarkPath = "{http://namespace1}elem1/{http://namespace2}elem2/{http://namespace1}elem3";
 * String clarkNames[] = ClarkName.splitPath(clarkPath);
 * </pre>
 * 
 * @author Santhosh Kumar T
 */
public class ClarkName{
    /**
     * Returns string representation as <a href="http://jclark.com/xml/xmlns.htm">defined</a> by James Clark.
     * <p>
     * This is same as {@code String} returned by {@link javax.xml.namespace.QName#toString()}.
     *
     * @param namespace namespace, can be null
     * @param localPart localpart
     *
     * @return clarkName
     */
    public static String valueOf(String namespace, String localPart){
        return StringUtil.isEmpty(namespace) ? localPart : '{'+namespace+'}'+localPart;
    }

    /**
     * Splits given {@code clarkName} to {@code namespace} and {@code localPart}
     *
     * @param clarkName clarkName to be split
     *
     * @return {@code String} array of size {@code 2}.
     *         First item is {@code namespace} and second item is {@code localPart}. 
     */
    public static String[] split(String clarkName){
        int end = clarkName.lastIndexOf('}');
        if(end==-1)
            return new String[]{ "", clarkName };
        else
            return new String[]{ clarkName.substring(1, end), clarkName.substring(end+1) };
    }

    /**
     * Splits given {@code clarkPath} into {@code clarkNames}.
     * <p>
     * {@code clarkPath} is a sequence of {@code clarkNames} separated by {@code /}.<br>
     * Example:
     * <pre class="prettyprint">
     * {http://namespace1}elem1/{http://namespace2}elem2/{http://namespace1}elem3
     * </pre>
     *
     * @param clarkPath clarkPath to be split
     *
     * @return {@code String} array containing the {@code clarkNames}
     */
    public static String[] splitPath(String clarkPath){
        Stack<String> tokens = new Stack<String>();
        boolean foundNamespace = false;
        StringTokenizer stok = new StringTokenizer(clarkPath, "/", true);
        while(stok.hasMoreTokens()){
            String token = stok.nextToken();
            if(foundNamespace)
                token = tokens.pop() + token;

            if(token.charAt(0)=='{')
                foundNamespace = true;
            if(token.indexOf('}')!=-1)
                foundNamespace = false;
            if(foundNamespace || !token.equals("/"))
                tokens.push(token);
        }
        return tokens.toArray(new String[tokens.size()]);
    }
}

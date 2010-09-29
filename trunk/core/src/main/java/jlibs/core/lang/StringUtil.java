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

package jlibs.core.lang;

import jlibs.core.graph.Convertor;
import jlibs.core.graph.Filter;
import jlibs.core.util.regex.TemplateMatcher;

import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class StringUtil{
    /**
     * if <code>obj</code> is null, returns empty string.
     * otherwise returns <code>obj.toString()</code>
     */
    public static String toString(Object obj){
        return obj==null ? "" : obj.toString();
    }

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

    /**
     * Converts first character in <code>str</code> to uppercase.
     * <p>
     * This method can be called on string of any length.
     *
     * @param str string to be converted
     * @return string with first letter changed to uppercase
     */
    public static String capitalize(String str){
        if(str==null)
            return null;
        switch(str.length()){
            case 0:
                return str;
            case 1:
                return str.toUpperCase();
            default:
                return Character.toUpperCase(str.charAt(0))+str.substring(1);
        }
    }

    /**
     * Makes an underscored form from the expression in the string.
     * <p>
     * Examples:
     * <pre class="prettyprint">
     * underscore("activeRecord")     // "active_record"
     * underscore("ActiveRecord")     // "active_record"
     * underscore("firstName")        // "first_name"
     * underscore("FirstName")        // "first_name"
     * underscore("name")             // "name"
     * </pre>
     *
     * @param camelCaseWord the camel-cased word that is to be converted;
     * @return a lower-cased version of the input, with separate words delimited by the underscore character.
     */
    public static String underscore(String camelCaseWord){
        if(camelCaseWord==null)
            return null;
        camelCaseWord = camelCaseWord.trim();
        if(camelCaseWord.length()==0)
            return "";
        camelCaseWord = camelCaseWord.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2");
        camelCaseWord = camelCaseWord.replaceAll("([a-z\\d])([A-Z])", "$1_$2");
        return camelCaseWord.toLowerCase();
    }

    /**
     * Turns a non-negative number into an ordinal string used to
     * denote the position in an ordered sequence, such as 1st, 2nd,
     * 3rd, 4th
     *
     * @param number the non-negative number
     * @return the string with the number and ordinal suffix
     */
    public static String ordinalize(int number){
        int modulo = number%100;
        if(modulo>=11 && modulo<=13)
            return number+"th";
        switch(number%10){
            case 1:
                return number+"st";
            case 2:
                return number+"nd";
            case 3:
                return number+"rd";
            default:
                return number+"th";
        }
    }

    public static int[] toCodePoints(String str){
        int count = str.codePointCount(0, str.length());
        int[] codePoints = new int[count];
        for(int cpIndex=0, charIndex=0; cpIndex<count; cpIndex++){
            int cp = str.codePointAt(charIndex);
            codePoints[cpIndex] = cp;
            charIndex += Character.charCount(cp);
        }
        return codePoints;
    }

    /*-------------------------------------------------[ Array Join ]---------------------------------------------------*/
    
    public static <T> String join(T[] array){
        return join(array, ", ", null);
    }

    public static <T> String join(T[] array, String separator){
        return join(array, separator, null);
    }

    public static <T> String join(T[] array, String separator, Convertor<T, String> convertor){
        StringBuilder buff = new StringBuilder();
        boolean addSeparator = false;
        for(T item: array){
            if(addSeparator)
                buff.append(separator);
            else
                addSeparator = true;
            if(item==null)
                buff.append("null");
            else
                buff.append(convertor==null ? item.toString() : convertor.convert(item));
        }
        return buff.toString();
    }

    /*-------------------------------------------------[ Iterator Join ]---------------------------------------------------*/

    public static <T> String join(Iterator<T> iter){
        return join(iter, ", ", null);
    }

    public static <T> String join(Iterator<T> iter, String separator){
        return join(iter, separator, null);
    }
    
    public static <T> String join(Iterator<T> iter, String separator, Convertor<T, String> convertor){
        StringBuilder buff = new StringBuilder();
        boolean addSeparator = false;
        while(iter.hasNext()){
            T item = iter.next();
            if(addSeparator)
                buff.append(separator);
            else
                addSeparator = true;
            if(item==null)
                buff.append("null");
            else
                buff.append(convertor==null ? item.toString() : convertor.convert(item));
        }
        return buff.toString();
    }

    /*-------------------------------------------------[ Literal ]---------------------------------------------------*/

    public static String toLiteral(char ch, boolean useRaw){
        if(ch=='\'')
            return "\\'";
        else if(ch=='"')
            return "\"";
        else
            return StringUtil.toLiteral(String.valueOf(ch), useRaw);
    }
    
    public static String toLiteral(CharSequence str, boolean useRaw){
        StringBuffer buf = new StringBuffer(str.length()+25);
        for(int i=0,len=str.length(); i<len; i++){
            char c = str.charAt(i);
            switch(c){
                case '\b': buf.append("\\b"); break;
                case '\t': buf.append("\\t"); break;
                case '\n': buf.append("\\n"); break;
                case '\f': buf.append("\\f"); break;
                case '\r': buf.append("\\r"); break;
                case '\"': buf.append("\\\""); break;
                case '\\': buf.append("\\\\"); break;
                default:
                    if(c>=0x0020 && (useRaw || c<=0x007f)) // visible character in ascii
                        buf.append(c);
                    else{
                        buf.append("\\u");
                        String hex = Integer.toHexString(c);
                        for(int j=4-hex.length(); j>0; j--)
                            buf.append('0');
                        buf.append(hex);
                    }
            }
        }
        return buf.toString();
    }

    public static String fromLiteral(String str){
        StringBuffer buf = new StringBuffer();

        for(int i=0,len=str.length(); i<len; i++){
            char c = str.charAt(i);

            switch(c){
                case '\\':
                    if(i == str.length()-1){
                        buf.append('\\');
                        break;
                    }
                    c = str.charAt(++i);
                    switch(c){
                        case 'n':
                            buf.append('\n');
                            break;
                        case 't':
                            buf.append('\t');
                            break;
                        case 'r':
                            buf.append('\r');
                            break;
                        case 'u':
                            int value = 0;
                            for(int j=0; j<4; j++){
                                c = str.charAt(++i);
                                switch(c){
                                    case '0': case '1': case '2': case '3': case '4':
                                    case '5': case '6': case '7': case '8': case '9':
                                        value = (value << 4) + c - '0';
                                        break;
                                    case 'a': case 'b': case 'c':
                                    case 'd': case 'e': case 'f':
                                        value = (value << 4) + 10 + c - 'a';
                                        break;
                                    case 'A': case 'B': case 'C':
                                    case 'D': case 'E': case 'F':
                                        value = (value << 4) + 10 + c - 'A';
                                        break;
                                    default:
                                        throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                                }
                            }
                            buf.append((char)value);
                            break;
                        default:
                            buf.append(c);
                            break;
                    }
                    break;
                default:
                    buf.append(c);
            }
        }
        return buf.toString();
    }
}

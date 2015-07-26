/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.xml;

import jlibs.core.lang.StringUtil;
import jlibs.core.util.NonNullIterator;

import java.util.Iterator;

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
 * Iterator<String> clarkNames = ClarkName.iterator(clarkPath);
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
     * Tokenizes given {@code clarkPath} into {@code clarkNames}.
     * <p>
     * {@code clarkPath} is a sequence of {@code clarkNames} separated by {@code /}.<br>
     * Example:
     * <pre class="prettyprint">
     * {http://namespace1}elem1/{http://namespace2}elem2/{http://namespace1}elem3
     * </pre>
     *
     * @param clarkPath clarkPath to be tokenized
     *
     * @return {@code String} Iterator containing the {@code clarkNames}
     */
    public static Iterator<String> iterator(final String clarkPath){
        return new NonNullIterator<String>(){
            int from = 0;
            @Override
            protected String findNext(){
                if(from==clarkPath.length())
                    return null;

                int searchFrom = from;
                if(clarkPath.charAt(from)=='{'){
                    searchFrom = clarkPath.indexOf('}', from);
                    if(searchFrom==-1)
                        throw new IllegalArgumentException("no matching brace for brace at "+from);
                }
                int slash = clarkPath.indexOf('/', searchFrom);
                int curFrom = from;
                if(slash==-1){
                    from = clarkPath.length();
                    return clarkPath.substring(curFrom);
                }else{
                    from = slash+1;
                    return clarkPath.substring(curFrom, slash);
                }
            }
        };
    }
}

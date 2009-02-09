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

package jlibs.xml.sax.sniff.model.expr.string;

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.expr.Function;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Translate extends Function{
    public Translate(Node contextNode){
        super(contextNode, Datatype.STRING, Datatype.STRING, Datatype.STRING, Datatype.STRING);
    }

    @Override
    protected Object evaluate(Object[] args){
        return translate((String)args[0], (String)args[1], (String)args[2]);
    }

    /*-------------------------------------------------[ Algorithm ]---------------------------------------------------*/
    
    public static String translate(String input, String from, String to){
        // Initialize the mapping in a HashMap
        Map<String, String> characterMap = new HashMap<String, String>();
        String[] fromCharacters = toUnicodeCharacters(from);
        String[] toCharacters = toUnicodeCharacters(to);
        int fromLen = fromCharacters.length;
        int toLen = toCharacters.length;
        for(int i=0; i<fromLen; i++){
            String cFrom = fromCharacters[i];
            if(characterMap.containsKey(cFrom)) // We've seen the character before, ignore
                continue;
            if(i<toLen) // Will change
                characterMap.put(cFrom, toCharacters[i]);
            else // Will delete
                characterMap.put(cFrom, null);
        }

        // Process the input string thru the map
        StringBuilder output = new StringBuilder(input.length());
        String[] inCharacters = toUnicodeCharacters(input);
        int inLen = inCharacters.length;
        for(int i=0; i<inLen; i++){
            String cIn = inCharacters[i];
            if(characterMap.containsKey(cIn)){
                String cTo = characterMap.get(cIn);
                if(cTo!=null)
                    output.append(cTo);
            }else
                output.append(cIn);
        }

        return output.toString();
    }

    private static String[] toUnicodeCharacters(String s){
        String[] result = new String[s.length()];
        int stringLength = 0;
        for(int i = 0; i < s.length(); i++){
            char c1 = s.charAt(i);
            if(isHighSurrogate(c1)){
                try{
                    char c2 = s.charAt(i+1);
                    if(isLowSurrogate(c2)){
                        result[stringLength] = (c1 + "" + c2).intern();
                        i++;
                    }else
                        throw new IllegalArgumentException("Mismatched surrogate pair in translate function");
                }catch (StringIndexOutOfBoundsException ex){
                    throw new IllegalArgumentException("High surrogate without low surrogate at end of string passed to translate function");
                }
            }else
                result[stringLength]=String.valueOf(c1).intern();
            stringLength++;
        }

        if(stringLength==result.length)
            return result;

        // trim array
        String[] trimmed = new String[stringLength];
        System.arraycopy(result, 0, trimmed, 0, stringLength);
        return trimmed;

    }

    private static boolean isHighSurrogate(char c){
        return c >= 0xD800 && c <= 0xDBFF;
    }

    private static boolean isLowSurrogate(char c){
        return c >= 0xDC00 && c <= 0xDFFF;
    }
}

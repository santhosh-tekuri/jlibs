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

import java.util.Arrays;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class JavaUtil{
    public static final List<String> KEYWORDS = Arrays.asList(
        "abstract", "assert",
        "boolean", "break", "byte",
        "case", "catch", "char", "class", "const", "continue",
        "default", "do", "double",
        "else", "enum", "extends",
        "false", "final", "finally", "float", "for",
        "goto",
        "if", "implements", "import", "instanceof", "int", "interface",
        "long",
        "native", "new", "null",
        "package", "private", "protected", "public",
        "return",
        "short", "static", "strictfp", "super", "switch", "synchronized",
        "this", "throw", "throws", "transient", "true", "try",
        "void", "volatile",
        "while"
    );

    public static boolean isIdentifier(CharSequence seq){
        int len = seq.length();
        if(len==0)
            return false;
        if(!Character.isJavaIdentifierStart(seq.charAt(0)))
            return false;
        for(int i=1; i<len; i++){
            if(!Character.isJavaIdentifierPart(seq.charAt(i)))
                return false;
        }
        return true;
    }
}

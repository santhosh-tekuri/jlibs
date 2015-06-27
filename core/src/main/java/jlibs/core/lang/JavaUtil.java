/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

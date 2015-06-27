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

package jlibs.core.util.regex;

import jlibs.core.io.FileUtil;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code TemplateMatcher} is a simple template engine provided with jlibs.
 * <pre class="prettyprint">
 * import jlibs.core.util.regex.TemplateMatcher;
 *
 * String msg = "Hai ${user}, your mail to ${email} has been sent successfully.";
 *
 * TemplateMatcher matcher = new TemplateMatcher("${", "}");
 *
 * Map<String, String> vars = new HashMap<String, String>();
 * vars.put("user", "santhosh");
 * vars.put("email", "scott@gmail.com");
 * System.out.println(matcher.replace(msg, vars));
 * </pre>
 * prints following:
 * <pre class="prettyprint">
 * Hai santhosh, your mail to scott@gmail.com has been sent successfully.
 * </pre>
 *
 * The two arguments to {@code TemplateMatcher} are {@code leftBrace} and {@code rightBrace}.<br>
 * For example:
 * <pre class="prettyprint">
 * String msg = "Hai ___user___, your mail to ___email___ has been sent successfully.";
 * TemplateMatcher matcher = new TemplateMatcher("___", "___");
 *
 * Map<String, String> vars = new HashMap<String, String>();
 * vars.put("user", "santhosh");
 * vars.put("email", "scott@gmail.com");
 * System.out.println(matcher.replace(msg, vars));
 * </pre>
 * also prints the same output.
 * <p>
 * <b>NOTE: </b>if a variables resolves to {@code null}, then it appears as it is in result string
 * </p>
 * Right Brace is optional. in such case use {@code new TemplateMatcher(leftBrace)}:
 * <pre class="prettyprint">
 * String msg = "Hai $user, your mail to $email has been sent successfully.";
 *
 * TemplateMatcher matcher = new TemplateMatcher("$");
 *
 * Map<String, String> vars = new HashMap<String, String>();
 * vars.put("user", "santhosh");
 * vars.put("email", "scott@gmail.com");
 * System.out.println(matcher.replace(msg, vars));
 * </pre>
 * also prints the same output;
 * <p>
 * <b>Variable Resolution:</b>
 * </p>
 * you can also resolve variables dynamically:
 * <pre class="prettyprint">
 * String msg = "Hai ${user.name}, you are using JVM from ${java.vendor}.";
 *
 * TemplateMatcher matcher = new TemplateMatcher("${", "}");
 * String result = matcher.replace(msg, new TemplateMatcher.VariableResolver(){
 *     &#064;Override
 *     public String resolve(String variable){
 *         return System.getProperty(variable);
 *     }
 * });
 * </pre>
 * prints
 * <pre class="prettyprint">
 * Hai santhosh, you are using JVM from Apple Inc..
 * </pre>
 * {@code VariableResolver} interface contains single method:
 * <pre class="prettyprint">
 * public String resolve(String variable)
 * </pre>
 * <p>
 * <b>Using with writers:</b>
 * </p>
 * Let us say you have file {@code template.txt} which contains:
 * <pre class="prettyprint">
 * Hai ${user},
 *     your mail to ${email} has been sent successfully.
 * </pre>
 * running the following code:
 * <pre class="prettyprint">
 * TemplateMatcher matcher = new TemplateMatcher("${", "}");
 *
 * Map<String, String> vars = new HashMap<String, String>();
 * vars.put("user", "santhosh");
 * vars.put("email", "scott@gmail.com");
 * matcher.replace(new FileReader("templte.txt"), new FileWriter("result.txt"), vars);
 * </pre>
 * will creates file {@code result.txt} with following content:
 * <pre class="prettyprint">
 * Hai santhosh,
 *     your mail to scott@gmail.com has been sent successfully.
 * </pre>
 * <p>
 * <b>Copying Files/Directories:</b>
 * </p>
 * {@code TemplateMatcher} provides method to copy files/directories:
 * <pre class="prettyprint">
 * public void copyInto(File source, File targetDir, Map<String, String> variables) throws IOException;
 * </pre>
 * Name of each file and directory is treated as a template.<br>
 * If name of directory is ${xyz} after applying template, if resolves to "a/b/c",<br>
 * then it expands into the directory structure a/b/c;
 *
 * for example we have following directory structure:
 * <pre class="prettyprint">
 * ${root}
 *   |- ${class}.java
 * </pre>
 * and content of ${class}.java file is:
 * <pre class="prettyprint">
 * package ${rootpackage};
 *
 * public class ${class} extends Comparator{
 *
 * }
 * </pre>
 * now running following code:
 * <pre class="prettyprint">
 * TemplateMatcher matcher = new TemplateMatcher("${", "}");
 *
 * Map<String, String> vars = new HashMap<String, String>();
 * vars.put("root", "org/example");
 * vars.put("rootpackage", "org.example");
 * vars.put("class", "MyClass");
 *
 * matcher.copyInto(new File("${root}"), new File("."), vars);
 * </pre>
 * creates:
 * <pre class="prettyprint">
 * org
 *   |-example
 *     |-MyClass.java
 * </pre>
 * and content of MyClass.java will be:
 * <pre class="prettyprint">
 * package org.example;
 *
 * public class MyClass extends Comparator{
 *
 * }
 * </pre>
 *
 * @author Santhosh Kumar T
 */
public class TemplateMatcher{
    private Pattern pattern;

    public TemplateMatcher(String leftBrace, String rightBrace){
        leftBrace = Pattern.quote(leftBrace);
        rightBrace = Pattern.quote(rightBrace);
        pattern = Pattern.compile(leftBrace+"(.*?)"+rightBrace);
    }

    public TemplateMatcher(String prefix){
        prefix = Pattern.quote(prefix);
        pattern = Pattern.compile(prefix+"(\\w*)");
    }

    /*-------------------------------------------------[ Replace ]---------------------------------------------------*/
    
    public String replace(CharSequence input, VariableResolver resolver){
        StringBuilder buff = new StringBuilder();

        Matcher matcher = pattern.matcher(input);
        int cursor = 0;
        while(cursor<input.length() && matcher.find(cursor)){
            buff.append(input.subSequence(cursor, matcher.start()));
            String value = resolver.resolve(matcher.group(1));
            buff.append(value!=null ? value : matcher.group());
            cursor = matcher.end();
        }
        buff.append(input.subSequence(cursor, input.length()));
        return buff.toString();
    }

    public String replace(String input, final Map<String, String> variables){
        return replace(input, new MapVariableResolver(variables));
    }

    /*-------------------------------------------------[ Character Streams ]---------------------------------------------------*/
    
    public void replace(Reader reader, Writer writer, VariableResolver resolver) throws IOException{
        BufferedReader breader = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
        BufferedWriter bwriter = writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer);
        try{
            boolean firstLine = true;
            for(String line; (line=breader.readLine())!=null;){
                if(firstLine)
                    firstLine = false;
                else
                    bwriter.newLine();
                bwriter.write(replace(line, resolver));
            }
        }finally{
            try{
                breader.close();
            }finally{
                bwriter.close();
            }
        }
    }


    public void replace(Reader reader, Writer writer, Map<String, String> variables) throws IOException{
        replace(reader, writer, new MapVariableResolver(variables));
    }

    /*-------------------------------------------------[ File Copy ]---------------------------------------------------*/
    
    public void copyInto(File source, File targetDir, final VariableResolver resolver) throws IOException{
        FileUtil.copyInto(source, targetDir, new FileUtil.FileCreator(){
            @Override
            public void createFile(File sourceFile, File targetFile) throws IOException{
                replace(new FileReader(sourceFile), new FileWriter(targetFile), resolver);
            }

            @Override
            public String translate(String name){
                return replace(name, resolver);
            }
        });
    }

    public void copyInto(File source, File targetDir, Map<String, String> variables) throws IOException{
        copyInto(source, targetDir, new MapVariableResolver(variables));
    }

    /*-------------------------------------------------[ VariableResolver ]---------------------------------------------------*/
    
    public static interface VariableResolver{
        public String resolve(String variable);
    }

    public static class MapVariableResolver implements VariableResolver{
        private Map<String, String> variables;

        public MapVariableResolver(Map<String, String> variables){
            this.variables = variables;
        }

        @Override
        public String resolve(String variable){
            return variables.get(variable);
        }
    }

    /*-------------------------------------------------[ Testing ]---------------------------------------------------*/

    public static void main(String[] args){
        System.out.println(new TemplateMatcher("${", "}").replace("this is ${santhosh}ghgjh\n ${kumar} sdf ${tekuri}abc", new VariableResolver(){
            @Override
            public String resolve(String variable){
                if(variable.equals("santhosh"))
                    return null;
                return variable.toUpperCase();
            }
        }));

        System.out.println(new TemplateMatcher("$").replace("this is $santhosh ghgjh\n $kumar sdf $tekuri\n$ abc", new VariableResolver(){
            @Override
            public String resolve(String variable){
                return variable.toUpperCase();
            }
        }));
    }
}

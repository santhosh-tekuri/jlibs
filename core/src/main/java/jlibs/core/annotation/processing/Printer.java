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

package jlibs.core.annotation.processing;

import jlibs.core.lang.model.ModelUtil;
import jlibs.core.util.regex.TemplateMatcher;

import javax.lang.model.element.TypeElement;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Printer{
    public static final String PLUS = "indent++";
    public static final String MINUS = "indent--";
    
    public TypeElement clazz;
    public String generatedQName;
    public String generatedPakage;
    public String generatedClazz;

    public int indent;
    PrintWriter ps;

    public Printer(PrintWriter ps){
        this.ps = ps;
    }

    private boolean doIndent = true;
    private void indent(){
        if(doIndent){
            if(emptyLine){
                println();
                emptyLine = false;
            }
            for(int i=0; i<indent; i++)
                ps.print("    ");
        }
    }
    public void println(String str){
        if(str.length()>0)
            indent();
        ps.println(str);
        doIndent = true;
    }

    public void println(String... tokens){
        for(String token: tokens)
            print(token);
        println();
    }
    
    @SuppressWarnings({"StringEquality"})
    public void printlns(String... lines){
        for(String line: lines){
            if(line==null)
                continue;
            if(line==PLUS)
                indent++;
            else if(line==MINUS)
                indent--;
            else
                println(line);
        }
    }

    public void printlns(InputStream is){
        printlns(is, null, null);
    }

    public void printlns(InputStream is, TemplateMatcher matcher, TemplateMatcher.VariableResolver variableResolver){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(is));
            for(String line=reader.readLine(); line!=null; line=reader.readLine()){
                if(matcher!=null)
                    line = matcher.replace(line, variableResolver);
                println(line);
            }
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }finally{
            if(reader!=null)
                try{
                    reader.close();
                }catch(IOException ex){
                    ex.printStackTrace();
                }
        }
    }

    public void print(String str){
        indent();
        ps.print(str);
        doIndent = false;
    }

    private boolean emptyLine;
    public void emptyLine(boolean reqd){
        emptyLine = reqd;
    }

    public void println(){
        ps.println();
        doIndent = true;
    }

    public void close(){
        ps.close();
    }
    
    private static Map<TypeElement, Printer> registry = new HashMap<TypeElement, Printer>();
    public static Printer get(TypeElement clazz, Class annotation, String format) throws IOException{
        Printer printer = registry.get(clazz);
        if(printer==null){
            String str[] = ModelUtil.findClass(clazz, format);
            if(ModelUtil.exists(str[1], str[2]+".java"))
                throw new AnnotationError(clazz, ModelUtil.getAnnotationMirror(clazz, annotation), "Class "+str[0]+" already exists in source path");
            PrintWriter writer = new PrintWriter(Environment.get().getFiler().createSourceFile(str[0]).openWriter());
            printer=new Printer(writer);
            printer.clazz = clazz;
            printer.generatedQName = str[0];
            printer.generatedPakage = str[1];
            printer.generatedClazz = str[2];
            registry.put(clazz, printer);
        }
        return printer;
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    public void printPackage(){
        if(generatedPakage !=null && generatedPakage.length()>0){
            println("package "+generatedPakage +";");
            emptyLine(true);
        }
    }

    public void importClass(Class clazz){
        println("import "+ clazz.getName()+';');
    }

    public void importClass(TypeElement clazz){
        println("import "+ModelUtil.toString(clazz.asType(), true)+";");
    }

    public void importPackage(Class clazz){
        Package pakage = clazz.getPackage();
        if(pakage!=null)
            println("import "+ pakage.getName()+".*;");
    }

    public void printClassDoc(){
        println("/**");
        println(" * DON'T EDIT THIS FILE. THIS IS GENERATED BY JLIBS");
        println(" *");
        println(" * @author Santhosh Kumar T");
        println(" */");
    }

    public void titleComment(String title){
        emptyLine(true);
        println("/*-------------------------------------------------[ "+title+" ]---------------------------------------------------*/");
        emptyLine(true);
    }

    public void printlnIf(String condition, String... body){
        printlnIf(condition, Arrays.asList(body));
    }
    
    public void printlnIf(String condition, List<String> body){
        printlns(
            "if("+condition+")"+(body.size()>1 ? "{" : ""),
                PLUS
        );
        printlns(body.toArray(new String[body.size()]));
        printlns(
            MINUS,
            body.size()>1 ? "}" : null
        );
    }

    public void printlnIf(String condition, List<String> ifBody, List<String> elseBody){
        if(ifBody.size()==1 && elseBody.size()==1 && ifBody.get(0).equals("return true;") && elseBody.get(0).equals("return false;"))
            println("return "+condition+";");
        else{
            printlns(
                "if("+condition+")"+(ifBody.size()>1 ? "{" : ""),
                    PLUS
            );
            printlns(ifBody.toArray(new String[ifBody.size()]));
            printlns(
                    MINUS,
                (ifBody.size()>1 ? "}" : "")+"else"+(elseBody.size()>1 ? "{" : ""),
                    PLUS
            );
            printlns(elseBody.toArray(new String[elseBody.size()]));
            printlns(
                    MINUS,
                elseBody.size()>1 ? "}" : null
            );
        }
    }
}

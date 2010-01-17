/*
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

package jlibs.core.annotation.processing;

import jlibs.core.lang.model.ModelUtil;

import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Printer{
    public TypeElement clazz;
    public String generatedPakage;
    public String generatedClazz;

    public int indent;
    PrintWriter ps;

    public Printer(int indent, PrintWriter ps){
        this.indent = indent;
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
        indent();
        ps.println(str);
        doIndent = true;
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
    public static Printer get(TypeElement clazz, String suffix) throws IOException{
        Printer printer = registry.get(clazz);
        if(printer==null){
            String generatedPakage = ModelUtil.getPackage(clazz);
            String generatedClazz = clazz.getSimpleName()+ suffix;

            String generatedClazzQName;
            if(generatedPakage==null||generatedPakage.length()==0)
                generatedClazzQName = generatedClazz;
            else
                generatedClazzQName = generatedPakage+"."+generatedClazz;

            PrintWriter writer = new PrintWriter(Environment.get().getFiler().createSourceFile(generatedClazzQName).openWriter());
            printer=new Printer(0, writer);
            printer.clazz = clazz;
            printer.generatedPakage = generatedPakage;
            printer.generatedClazz = generatedClazz;
            registry.put(clazz, printer);
        }
        return printer;
    }
}

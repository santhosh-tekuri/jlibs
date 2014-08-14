/*
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

package jlibs.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import static java.nio.channels.SelectionKey.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Debugger{
    public static final boolean DEBUG = false;
    public static final boolean IO = false;
    public static final boolean HTTP = false;
    static{
        System.setErr(System.out);
    }

    public static void println(String msg){
        println(msg, System.out);
    }

    public static void println(Object obj){
        println(String.valueOf(obj));
    }

    public static void println(String msg, PrintStream ps){
        Indentation indent = indentation.get();
        if(msg.equals("}"))
            indent.decrement();
        BufferedReader reader = new BufferedReader(new StringReader(msg));
        String line;
        try{
            while((line=reader.readLine())!=null){
                indent.print(ps);
                ps.println(line);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        if(msg.endsWith("{"))
            indent.increment();
    }

    public static String ops(int ops){
        String str = "";
        if((ops&OP_CONNECT)!=0)
            str += "C";
        if((ops&OP_ACCEPT)!=0)
            str += "A";
        if((ops&OP_READ)!=0)
            str += "R";
        if((ops&OP_WRITE)!=0)
            str += "W";
        return str;
    }

    private static final ThreadLocal<Indentation> indentation = new ThreadLocal<Indentation>(){
        @Override
        protected Indentation initialValue(){
            return new Indentation();
        }
    };

    private static class Indentation{
        private int amount;

        public void increment(){
            amount++;
        }

        public void decrement(){
            amount--;
        }

        public void print(PrintStream ps){
            String executionID = "";
            Reactor reactor = Reactor.current();
            if(reactor!=null)
                executionID = reactor.getExecutionID();
            ps.printf("%-20s", "["+executionID+"]");
            for(int i=0; i<amount; i++)
                ps.print("  ");
        }
    }
}

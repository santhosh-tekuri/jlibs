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

import java.io.PrintStream;

/**
 * Ansi coloring support is provided by this class.
 * <p><br>
 * To print "hello ansi world" in bold with blue foreground and white background:
 * <pre>
 * Ansi ansi = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.BLUE, Ansi.Color.WHITE);
 * ansi.{@link #out(String) out}("hello ansi world")
 * </pre>
 *
 * same can be done as below:
 * <pre>
 * String msg = ansi.{@link #colorize(String) colorize}("hello ansi world"); // msg is original string wrapped with ansi control sequences
 * System.out.println(msg);
 * </pre>
 *
 * <b>Ansi Support:</b>
 * <p><br>
 * Ansi might not be supported on all systems. Ansi is mostly supported by all unix operating systems.
 * <br><br>
 * {@link Ansi#SUPPORTED} is a final boolean, that can be used to check whether your console supports Ansi format;
 * <br><br>
 * Ansi class uses simple checks to decide whether ansi is supported or not. Sometimes it may do wrong guess.
 * In such cases you can override its decision using following system property:
 * <code>-DAnsi=true</code> or <code>-DAnsi=false</code>
 * <br><br>
 * if {@link Ansi#SUPPORTED} is false, any ansi method will not produce ansi control sequences. so you can safely use:
 * <code>ansi.out("hello ansi world")</code> irrespective of ansi is supported or not.
 * if ansi is not supported, this will simply do <code>System.out.print("hello ansi world")</code>
 * 
 * @author Santhosh Kumar T
 */
public class Ansi{
    /**
     * specifies whether ansi is supported or not.
     * <p><br> 
     * when this is false, it doesn't colorize given strings, rather than
     * simply returns the given strings
     * <p><br>
     * It tries best effort to guess whether ansi is supported or not. But
     * you can override this value using system property "Ansi" (-DAnsi=true/false)
     */
    public static final boolean SUPPORTED = Boolean.getBoolean("Ansi") || (OS.get().isUnix() && System.console()!=null);

    /** this enum represents the attribute of text */
    public enum Attribute{
        /** Reset All Attributes (return to normal mode) */
        NORMAL(0),
        /** Usually turns on BOLD */
        BRIGHT(1),
        DIM(2),
        UNDERLINE(4),
        BLINK(5),
        /** Reverse video on */
        REVERSE(7),
        /** Concealed on */
        HIDDEN(8);

        private String value;

        private Attribute(int value){
            this.value = String.valueOf(value);
        }

        public String toString(){
            return ""+value;
        }
    }

    /** this enum represents the color of text */
    public enum Color{ BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE }

    private static final String PREFIX = "\u001b["; //NOI18N
    private static final String SUFFIX = "m";
    private static final String SEPARATOR = ";";
    private static final String END = PREFIX + SUFFIX;

    private String start = "";

    /**
     * Creates new instanceof Ansi. 
     *
     * @param attr        attribute of text, null means don't change
     * @param foreground  foreground color of text, null means don't change
     * @param background  background color of text, null means don't change
     */
    public Ansi(Attribute attr, Color foreground, Color background){
        StringBuilder buff = new StringBuilder();

        if(attr!=null)
            buff.append(attr);

        if(foreground!=null){
            if(buff.length()>0)
                buff.append(SEPARATOR);
            buff.append(30+foreground.ordinal());
        }
        if(background!=null){
            if(buff.length()>0)
                buff.append(SEPARATOR);
            buff.append(40+background.ordinal());
        }
        buff.insert(0, PREFIX);
        buff.append(SUFFIX);
        
        start = buff.toString();
    }

    /** Wrapps given <code>message</message> with special ansi control sequences and returns it */
    public String colorize(String message){
        if(SUPPORTED){
            StringBuilder buff = new StringBuilder(start.length()+message.length()+END.length());
            buff.append(start).append(message).append(END);
            return buff.toString();
        }else
            return message;
    }

    /*-------------------------------------------------[ Printing ]---------------------------------------------------*/
    
    public void print(PrintStream ps, String message){
        if(SUPPORTED)
            ps.print(start);
        ps.print(message);
        if(SUPPORTED)
            ps.print(END);
    }

    public void println(PrintStream ps, String message){
        print(ps, message);
        System.out.println();
    }

    public void format(PrintStream ps, String message, Object... args){
        if(SUPPORTED)
            ps.print(start);
        ps.format(message, args);
        if(SUPPORTED)
            ps.print(END);
    }

    /*-------------------------------------------------[ System.out ]---------------------------------------------------*/

    public void out(String message){
        print(System.out, message);
    }

    public void outln(String message){
        println(System.out, message);
    }

    public void outFormat(String message, Object... args){
        format(System.out, message, args);
    }

    /*-------------------------------------------------[ System.err ]---------------------------------------------------*/
    
    public void err(String message){
        print(System.err, message);
    }

    public void errln(String message){
        print(System.err, message);
    }

    public void errFormat(String message, Object... args){
        format(System.err, message, args);
    }
}

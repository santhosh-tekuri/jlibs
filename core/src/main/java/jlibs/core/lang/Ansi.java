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
 * <p>
 * To print "hello ansi world" in bold with blue foreground and white background:
 * <pre class="prettyprint">
 * Ansi ansi = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.BLUE, Ansi.Color.WHITE);
 * ansi.{@link #out(String) out}("hello ansi world")
 * </pre>
 *
 * same can be done as below:
 * <pre class="prettyprint">
 * String msg = ansi.{@link #colorize(String) colorize}("hello ansi world"); // msg is original string wrapped with ansi control sequences
 * System.out.println(msg);
 * </pre>
 *
 * <b>Ansi Support:</b>
 * <p>
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
 * @see jlibs.core.util.logging.AnsiFormatter
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
        init(attr, foreground, background);
    }

    /**
     * Creates new instanceof of ansi with specified format.<p>
     * The format syntax is
     * <pre>
     * Attribute[;Foreground[;Background]]
     * </pre>
     * i.e, semicolon(;) separated values, where tokens are attribute, foreground and background respectively.<br>
     * if any non-trailing token in value is null, you still need to specify empty value. for example:
     * <pre>
     * DIM;;GREEN # foreground is not specified
     * </pre>
     * 
     * @param format
     */
    public Ansi(String format){
        String tokens[] = format.split(";");

        Ansi.Attribute attribute = null;
        try{
            if(tokens.length>0 && tokens[0].length()>0)
                attribute = Ansi.Attribute.valueOf(tokens[0]);
        }catch(IllegalArgumentException ex){
            ex.printStackTrace();
        }

        Ansi.Color foreground = null;
        try{
            if(tokens.length>1 && tokens[1].length()>0)
                foreground = Ansi.Color.valueOf(tokens[1]);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }

        Ansi.Color background = null;
        try{
            if(tokens.length>2 && tokens[2].length()>0)
                background = Ansi.Color.valueOf(tokens[2]);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }

        init(attribute, foreground, background);
    }

    private void init(Attribute attr, Color foreground, Color background){
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

    /**
     * The string representation of this object. This string will be the same that is
     * expected by {@link #Ansi(String)}
     *
     * @return string representation of this object
     */
    @Override
    public String toString(){
        Attribute attr = null;
        Color foreground = null;
        Color background = null;
        
        for(String token: start.substring(PREFIX.length(), start.length()-SUFFIX.length()).split(SEPARATOR)){
            int i = Integer.parseInt(token);
            if(i<30){
                for(Attribute value: Attribute.values()){
                    if(value.toString().equals(token)){
                        attr = value;
                        break;
                    }
                }
            }else if(i<40)
                foreground = Color.values()[i-30];
            else
                background = Color.values()[i-40];
        }


        StringBuilder buff = new StringBuilder();
        if(attr!=null)
            buff.append(attr.name());
        buff.append(';');
        if(foreground!=null)
            buff.append(foreground.name());
        buff.append(';');
        if(background!=null)
            buff.append(background.name());

        int end = buff.length()-1;
        while(end>=0 && buff.charAt(end)==';')
            end--;
        return buff.substring(0, end+1);
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

    /**
     * Prints colorized {@code message} to specified {@code ps}.
     * <p>
     * if {@link #SUPPORTED} is false, it prints raw {@code message} to {@code ps}
     *
     * @param ps        stream to print
     * @param message   message to be colorized
     */
    public void print(PrintStream ps, String message){
        if(SUPPORTED)
            ps.print(start);
        ps.print(message);
        if(SUPPORTED)
            ps.print(END);
    }

    /**
     * Prints colorized {@code message} to specified {@code ps} followed by newline.
     * <p>
     * if {@link #SUPPORTED} is false, it prints raw {@code message} to {@code ps} followed by newline.
     *
     * @param ps        stream to print
     * @param message   message to be colorized
     */
    public void println(PrintStream ps, String message){
        print(ps, message);
        ps.println();
    }

    /**
     * Prints formatted and colorized {@code message} to specified {@code ps}.
     * <p>
     * if {@link #SUPPORTED} is false, it prints formatted {@code message} to {@code ps}
     *
     * @param ps        stream to print
     * @param format    A format string whose output to be colorized
     * @param args      Arguments referenced by the format specifiers in the format
     */
    public void format(PrintStream ps, String format, Object... args){
        if(SUPPORTED)
            ps.print(start);
        ps.format(format, args);
        if(SUPPORTED)
            ps.print(END);
    }

    /*-------------------------------------------------[ System.out ]---------------------------------------------------*/

    /**
     * Prints colorized {@code message} to {@link System#out}
     *
     * @param message message to be colorized
     */
    public void out(String message){
        print(System.out, message);
    }

    /**
     * Prints colorized {@code message} to {@link System#out} followed by newline
     *
     * @param message message to be colorized
     */
    public void outln(String message){
        println(System.out, message);
    }

    /**
     * Prints formatted and colorized {@code format} to {@link System#out}
     *
     * @param format  A format string whose output to be colorized
     * @param args      Arguments referenced by the format specifiers in the format
     */
    public void outFormat(String format, Object... args){
        format(System.out, format, args);
    }

    /*-------------------------------------------------[ System.err ]---------------------------------------------------*/
    
    /**
     * Prints colorized {@code message} to {@link System#err}
     *
     * @param message message to be colorized
     */
    public void err(String message){
        print(System.err, message);
    }

    /**
     * Prints colorized {@code message} to {@link System#err} followed by newline
     *
     * @param message message to be colorized
     */
    public void errln(String message){
        print(System.err, message);
    }

    /**
     * Prints formatted and colorized {@code format} to {@link System#err}
     *
     * @param format  A format string whose output to be colorized
     * @param args      Arguments referenced by the format specifiers in the format
     */
    public void errFormat(String format, Object... args){
        format(System.err, format, args);
    }
}

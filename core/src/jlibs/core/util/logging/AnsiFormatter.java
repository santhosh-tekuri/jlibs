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

package jlibs.core.util.logging;

import jlibs.core.lang.Ansi;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.util.CollectionUtil;
import jlibs.core.io.FileUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.*;
import java.io.IOException;
import java.io.File;
import java.net.URL;

/**
 * Adds color to the standard Ant output by prefixing and suffixing ANSI color code escape sequences to it
 *
 * You can override default colors using a system variable named ansiformatter.defaults
 * for ex: -Dansiformatter.defaults=/path/to/your/file
 *
 * this file is a standard properties file, with entries of the form:
 *    LEVEL=Attribute[;Foreground[;Background]]
 *
 * for example:
 *          SEVERE=DIM;RED
 *
 * see jlibs.core.lang.Ansi for supported values of Attribute and Color
 *
 * @author Santhosh Kumar T
 */
public class AnsiFormatter extends Formatter{
    private static final Map<Level, Ansi> map = new LinkedHashMap<Level, Ansi>();
    static{
        try{
            load(AnsiFormatter.class.getResource("ansiformatter.properties"));
        }catch(IOException ex){
            throw new ImpossibleException(ex);
        }
        
        String file = System.getProperty("ansiformatter.default");
        try{
            if(file!=null && new File(file).exists())
                load(FileUtil.toURL(new File(file)));

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public static final Ansi SEVERE  = map.get(Level.SEVERE);
    public static final Ansi WARNING = map.get(Level.WARNING);
    public static final Ansi INFO    = map.get(Level.INFO);
    public static final Ansi CONFIG  = map.get(Level.CONFIG);
    public static final Ansi FINE    = map.get(Level.FINE);
    public static final Ansi FINER   = map.get(Level.FINER);
    public static final Ansi FINEST  = map.get(Level.FINEST);

    private static void load(URL url) throws IOException{
        Properties props = CollectionUtil.readProperties(url.openStream(), null);
        for(String name: props.stringPropertyNames()){
            Level level = Level.parse(name);
            String tokens[] = props.getProperty(name).split(";");

            Ansi.Attribute attribute = null;
            try{
                if(tokens.length==1)
                    attribute = Ansi.Attribute.valueOf(tokens[0]);
            }catch(IllegalArgumentException ex){
                ex.printStackTrace();
            }

            Ansi.Color foreground = null;
            try{
                if(tokens.length==2)
                    foreground = Ansi.Color.valueOf(tokens[1]);
            }catch(IllegalArgumentException e){
                e.printStackTrace();
            }

            Ansi.Color background = null;
            try{
                if(tokens.length==3)
                    foreground = Ansi.Color.valueOf(tokens[2]);
            }catch(IllegalArgumentException e){
                e.printStackTrace();
            }

            map.put(level, new Ansi(attribute, foreground, background));
        }
    }
    
    private Formatter delegate;

    public AnsiFormatter(Formatter delegate){
        this.delegate = delegate;
    }

    public AnsiFormatter(){
        this(new PreciseFormatter());
    }

    @Override
    public String format(LogRecord record){
        return map.get(record.getLevel()).colorize(delegate.format(record));
    }

    public static void main(String[] args){
        Logger logger = LogManager.getLogManager().getLogger("");
        logger.setLevel(Level.FINEST);
        Handler handler = logger.getHandlers()[0];
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new AnsiFormatter());
        for(Level level: map.keySet())
            logger.log(level, "this is "+level+" message");
    }
}


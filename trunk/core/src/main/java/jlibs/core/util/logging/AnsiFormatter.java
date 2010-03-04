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

package jlibs.core.util.logging;

import jlibs.core.io.FileUtil;
import jlibs.core.lang.Ansi;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.util.CollectionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This is an implementation of {@link java.util.logging.Formatter}, to use ansi colors in logging.
 * <p>
 * Example Usage:
 * <pre>
 * Logger logger = LogManager.getLogManager().getLogger("");
 * logger.setLevel(Level.FINEST);
 *
 * Handler handler = logger.getHandlers()[0];
 * handler.setLevel(Level.FINEST);
 * handler.setFormatter(new {@link AnsiFormatter}());
 *
 * for(Level level: map.keySet())
 *     logger.log(level, "this is "+level+" message"); * </pre>
 * </pre>
 *
 * This class has public final constants to access Ansi instance used for each level.<br>
 * These constants are made public, so that you can use them any where. for example you can do:
 * <pre>
 * import static jlibs.core.util.logging.AnsiFormatter.*;
 *
 * {@link #SEVERE}.out("User authentication failed");
 * </pre>
 *
 * The colors used by AnsiFormatter for any level can be changed to match you taste. To do this you need to create a properties file.<br>
 * for example:
 * <pre>
 * # myansi.properties
 * SEVERE=DIM;RED;GREEN
 * WARNING=BRIGHT;RED;YELLOW
 * </pre>
 * Now use following system property:
 * <pre>
 * -Dansiformatter.default=/path/to/myansi.properties
 * </pre>
 * Each entry in this property file is to be given as below:
 * <pre>
 * LEVEL=Attribute[;Foreground[;Background]]
 * </pre>
 * key is the level name;<br>
 * value is semicolon(;) separated values, where each argument is considered as argument to Ansi class constructor.<br>
 * if any agument in value is null, you still need to specify empty argument. for example:
 * <pre>
 * SEVERE=DIM;;GREEN # foreground is not specified
 * </pre>
 * In your properties file, you don't need to specify entries for each level. you can specify entries only for those levels that you want to change.
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
}


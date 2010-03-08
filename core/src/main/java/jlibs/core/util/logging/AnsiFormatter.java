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
 * <pre class="prettyprint">
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
 * <pre class="prettyprint">
 * import static jlibs.core.util.logging.AnsiFormatter.*;
 *
 * {@link #SEVERE}.out("User authentication failed");
 * </pre>
 *
 * The colors used by AnsiFormatter for any level can be changed to match you taste. To do this you need to create a properties file.<br>
 * for example:
 * <pre class="prettyprint">
 * # myansi.properties
 * SEVERE=DIM;RED;GREEN
 * WARNING=BRIGHT;RED;YELLOW
 * </pre>
 * Now use following system property:
 * <pre class="prettyprint">
 * -Dansiformatter.default=/path/to/myansi.properties
 * </pre>
 * Each entry in this property file is to be given as below:
 * <pre class="prettyprint">
 * LEVEL=Attribute[;Foreground[;Background]]
 * </pre>
 * key is the level name;<br>
 * value is semicolon(;) separated values, where where tokens are attribute, foreground and background respectively.<br>
 * if any non-trailing token in value is null, you still need to specify empty value. for example:
 * <pre class="prettyprint">
 * SEVERE=DIM;;GREEN # foreground is not specified
 * </pre>
 * In your properties file, you don't need to specify entries for each level. you can specify entries only for those levels that you want to change.
 *
 * @see jlibs.core.lang.Ansi
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
        for(String name: props.stringPropertyNames())
            map.put(Level.parse(name), new Ansi(props.getProperty(name)));
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


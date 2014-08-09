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

package jlibs.nio.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Santhosh Kumar Tekuri
 */
public class LogHandler{
    private File dir;
    private String prefix;
    private String suffix;
    private String format;
    private RepeatingDuration repeatingDuration;
    private long next = 0;
    private BufferedWriter writer;

    public LogHandler(File dir, String prefix, String suffix, String format){
        this.dir = dir;
        this.prefix = prefix;
        this.suffix = suffix;
        this.format = format;
        repeatingDuration = RepeatingDuration.forFormat(format);
    }

    private void rotateIfNecessary(){
        try{
            long now = System.currentTimeMillis();
            if(now>=next){
                if(writer !=null)
                    writer.close();
                File file = new File(dir, prefix+new SimpleDateFormat(format).format(new Date(now))+suffix);
                writer = new BufferedWriter(new FileWriter(file));
                next = repeatingDuration.next();
            }
        }catch(Throwable ex){
            ex.printStackTrace();
        }
    }

    public synchronized void publish(Record record){
        try{
            rotateIfNecessary();
            record.publish(writer);
            writer.flush();
        }catch(Throwable ex){
            ex.printStackTrace();
        }
    }

    public static interface Record{
        public void publish(Appendable writer) throws IOException;
    }
}

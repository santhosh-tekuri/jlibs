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

import jlibs.core.io.IOPump;
import jlibs.core.io.IOUtil;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class RuntimeUtil{
    /**
     * Redirects processes input and error streams to the specified streams.
     * the streams specified are not closed automatically. The streams passed
     * can be null, if you don't want to redirect them.
     */
    public static void redirectStreams(Process process, OutputStream output, OutputStream error){
        if(output!=null)
            new Thread(new IOPump(process.getInputStream(), output, false, false).asRunnable()).start();
        if(error!=null)
            new Thread(new IOPump(process.getErrorStream(), error, false, false).asRunnable()).start();
    }

    /*-------------------------------------------------[ Terminal Command ]---------------------------------------------------*/

    public static String runCommand(String command, String[] envp, File workingDir) throws IOException{
        String cmd[];
        if(OS.get().isUnix())
            cmd = new String[]{ "sh", "-c", command };
        else
            cmd = new String[]{ "cmd", "/C", command };

        Process p = Runtime.getRuntime().exec(cmd, envp, workingDir);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        redirectStreams(p, output, System.err);
        try{
            p.waitFor();
        }catch(InterruptedException ex){
            throw new RuntimeException("interrupted", ex);
        }
        if(p.exitValue()!=0)
            throw new IOException("exitValue is "+p.exitValue());

        return output.toString();
    }

    public static String runCommand(String command) throws IOException{
        return runCommand(command, null, null);
    }

    /*-------------------------------------------------[ PID ]---------------------------------------------------*/
    
    public static String getPID() throws IOException{
        String pid = System.getProperty("pid"); //NOI18N
        if(pid==null){
            String command;
            if(OS.get().isUnix())
                command = "echo $$ $PPID";
            else{
                // getpids.exe is taken from http://www.scheibli.com/projects/getpids/index.html (GPL)
                File tempFile = File.createTempFile("getpids", "exe"); //NOI18N

                // extract the embedded getpids.exe file from the jar and save it to above file
                IOUtil.pump(RuntimeUtil.class.getResourceAsStream("getpids.exe"), new FileOutputStream(tempFile), true, true); //NOI18N
                command = tempFile.getAbsolutePath();
                tempFile.deleteOnExit();
            }
            StringTokenizer stok = new StringTokenizer(runCommand(command));
            stok.nextToken(); // this is pid of the process we spanned
            pid = stok.nextToken();
            System.setProperty("pid", pid); //NOI18N
        }
        return pid;
    }

    /*-------------------------------------------------[ Garbage Collection ]---------------------------------------------------*/

    /**
     * This method guarantees that garbage collection is
     * done unlike <code>{@link System#gc()}</code>
     */
    public static void gc(){
        Object obj = new Object();
        WeakReference ref = new WeakReference<Object>(obj);
        obj = null;
        while(ref.get()!=null)
            System.gc();
    }

    /**
     * calls <code>{@link #gc()}</code> <code>count</code> times
     */
    public static void gc(int count){
        for(;count!=0; count--)
            gc();
    }

    /**
     * This method guarantees that garbage colleciton is
     * done after JVM shutdown is initialized
     */
    public static void gcOnExit(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                gc();
            }
        });
    }
}

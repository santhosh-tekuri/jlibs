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

package jlibs.core.io;

import jlibs.core.graph.Path;
import jlibs.core.graph.Processor;
import jlibs.core.graph.Walker;
import jlibs.core.graph.WalkerUtil;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.core.lang.ImpossibleException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

/**
 * @author Santhosh Kumar T
 */
public class FileUtil{
    public static final String PATH_SEPARATOR = File.pathSeparator;
    public static final String SEPARATOR = File.separator;
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final File JAVA_HOME = new File(System.getProperty("java.home"));
    public static final File USER_HOME = new File(System.getProperty("user.home"));
    public static final File USER_DIR = new File(System.getProperty("user.dir"));
    public static final File TMP_DIR = new File(System.getProperty("java.io.tmpdir"));

    public static @NotNull URL toURL(@NotNull File file){
        try{
            return file.toURI().toURL();
        }catch(MalformedURLException ex){
            throw new ImpossibleException(ex);
        }
    }

    public static File findFreeFile(File file){
        if(!file.exists())
            return file;

        String fileName = file.getName();
        int dot = fileName.lastIndexOf('.');
        if(dot==-1){
            int i = 0;
            File dir = file.getParentFile();
            while(true){
                i++;
                file = new File(dir, fileName+i);
                if(!file.exists())
                    return file;
            }
        }else{
            String name = fileName.substring(0, dot);
            String ext = fileName.substring(dot+1);

            int i = 0;
            File dir = file.getParentFile();
            while(true){
                i++;
                file = new File(dir, name+i+'.'+ext);
                if(!file.exists())
                    return file;
            }
        }
    }

    /*-------------------------------------------------[ Delete ]---------------------------------------------------*/

    /**
     * deletes specified file or directory
     * if given file/dir doesn't exist, simply returns
     */
    public static void delete(@NotNull File file) throws IOException{
        if(!file.exists())
            return;
        
        if(file.isFile()){
            if(!file.delete())
                throw new IOException("couldn't delete file :"+file);
        }else{
            try{
                WalkerUtil.walk(new PreorderWalker<File>(file, FileNavigator.INSTANCE), new Processor<File>(){
                    @Override
                    public boolean preProcess(File file, Path path){
                        return true;
                    }

                    @Override
                    public void postProcess(File file, Path path){
                        try{
                            if(!file.delete())
                                throw new IOException("couldn't delete file :"+file);
                        }catch(IOException ex){
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }catch(RuntimeException ex){
                if(ex.getCause() instanceof IOException)
                    throw (IOException)ex.getCause();
                else
                    throw ex;
            }
        }
    }

    public static void deleteEmptyDirs(File directory) throws IOException{
        if(directory.isFile())
            return;
        Walker<File> walker = new PreorderWalker<File>(directory, new FileNavigator(new FileFilter(){
            @Override
            public boolean accept(File file){
                return file.isDirectory();
            }
        }));
        try{
            WalkerUtil.walk(walker, new Processor<File>(){
                @Override
                public boolean preProcess(File file, Path path){
                    return true;
                }

                @Override
                public void postProcess(File file, Path path){
                    try{
                        if(file.list().length==0)
                            delete(file);
                    }catch(IOException ex){
                        throw new RuntimeException(ex);
                    }
                }
            });
        }catch(RuntimeException ex){
            if(ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            else
                throw ex;
        }
    }

    /*-------------------------------------------------[ MkDir ]---------------------------------------------------*/

    /**
     * create specified directory if doesn't exist.
     */
    public static void mkdir(File dir) throws IOException{
        if(!dir.exists() && !dir.mkdir())
            throw new IOException("couldn't create directory: "+dir);
    }

    /**
     * create specified directory if doesn't exist.
     * if any parent diretories doesn't exist they will
     * be created implicitly
     */
    public static void mkdirs(File dir) throws IOException{
        if(!dir.exists() && !dir.mkdirs())
            throw new IOException("couldn't create directory: "+dir);
    }

    /*-------------------------------------------------[ Copy ]---------------------------------------------------*/
    
    public static interface FileCreator{
        public void createFile(File sourceFile, File targetFile) throws IOException;
        public String translate(String name);
    }

    private static final FileCreator CREATOR = new FileCreator(){
        public void createFile(File sourceFile, File targetFile) throws IOException{
            mkdirs(targetFile.getParentFile());
            IOUtil.pump(new FileInputStream(sourceFile), new FileOutputStream(targetFile), true, true);
        }

        public String translate(String name){
            return name;
        }
    };

    public static void copyInto(File source, final File targetDir) throws IOException{
        copyInto(source, targetDir, CREATOR);
    }

    public static void copy(File source, final File target) throws IOException{
        copy(source, target, CREATOR);
    }

    public static void copyInto(File source, File targetDir, FileCreator creator) throws IOException{
        File target = new File(targetDir,creator.translate(source.getName()));
        copy(source, target, creator);
    }

    public static void copy(File source, final File target, final FileCreator creator) throws IOException{
        if(source.isFile())
            creator.createFile(source, target);
        else{
            try{
                mkdirs(target);

                Walker<File> walker = new PreorderWalker<File>(source, FileNavigator.INSTANCE);
                walker.next();
                final Stack<File> stack = new Stack<File>();
                stack.push(target);
                WalkerUtil.walk(walker, new Processor<File>(){
                    @Override
                    public boolean preProcess(File source, Path path){
                        File result = stack.peek();
                        result = new File(result, creator.translate(source.getName()));
                        stack.push(result);
                        try{
                            if(source.isDirectory())
                                mkdirs(result);
                            else
                                creator.createFile(source, result);
                        }catch(IOException ex){
                            throw new RuntimeException(ex);
                        }
                        return true;
                    }

                    @Override
                    public void postProcess(File source, Path path){
                        stack.pop();
                    }
                });
            }catch(IOException ex){
                if(ex.getCause() instanceof IOException)
                    throw (IOException)ex.getCause();
                else
                    throw ex;
            }
        }
    }

    public static void main(String[] args) throws IOException{
        copyInto(new File("/Users/santhosh/Downloads/xml-schemas"), new File("/Users/santhosh/Downloads/Incomplete"));
    }
}

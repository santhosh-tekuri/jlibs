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

package jlibs.core.io;

import jlibs.core.graph.*;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;

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

    public static URL toURL(File file){
        try{
            return file.toURI().toURL();
        }catch(MalformedURLException ex){
            throw new ImpossibleException(ex);
        }
    }

    /*-------------------------------------------------[ Name and Extension ]---------------------------------------------------*/

    /**
     * splits given fileName into name and extension.
     *
     * @param fileName fileName
     * @return string array is of length 2, where 1st item is name and 2nd item is extension(null if no extension)
     */
    public static String[] split(String fileName){
        int dot = fileName.lastIndexOf('.');
        if(dot==-1)
            return new String[]{ fileName, null };
        else
            return new String[] { fileName.substring(0, dot), fileName.substring(dot+1)};
    }

    /** Returns name of the file without extension */
    public static String getName(String fileName){
        int dot = fileName.lastIndexOf('.');
        return dot==-1 ? fileName : fileName.substring(0, dot);
    }

    /** Returns extension of the file. Returns null if there is no extension */
    public static String getExtension(String fileName){
        int dot = fileName.lastIndexOf('.');
        return dot==-1 ? null : fileName.substring(dot+1);
    }

    /*-------------------------------------------------[ Find Free File ]---------------------------------------------------*/

    /**
     * Finds a free file (i.e non-existing) in specified directory, using specified pattern.
     * for example:
     *      findFreeFile(myDir, "sample${i}.xml", false)
     *
     * will search for free file in order:
     *      sample1.xml, sample2.xml, sample3.xml and so on
     *
     * if tryEmptyVar is true, it it seaches for free file in order:
     *      sample.xml, sample2.xml, sample3.xml and so on
     *
     * the given pattern must have variable part ${i}
     */
    public static File findFreeFile(final File dir, String pattern, boolean tryEmptyVar){
        String name = StringUtil.suggest(new Filter<String>(){
            @Override
            public boolean select(String name){
                return !new File(dir, name).exists();
            }
        }, pattern, tryEmptyVar);

        return new File(dir, name);
    }

    /**
     * if file doesn't exist, it returns the same file. otherwize, it will find free
     * file as follows:
     *
     * if given file name is test.txt, then it searches for non existing file in order:
     *      test2.txt, test3.txt, test4.txt and so on
     *
     * if given file name is test(i,e with no extension), then it searches for non existing file in order:
     *      test2, test3, test4 and so on
     */
    public static File findFreeFile(File file){
        if(!file.exists())
            return file;

        String parts[] = split(file.getName());
        String pattern = parts[1]==null ? parts[0]+"${i}" : parts[0]+"${i}."+parts[1];
        return findFreeFile(file.getParentFile(), pattern, true);
    }

    /*-------------------------------------------------[ Delete ]---------------------------------------------------*/

    /**
     * deletes specified file or directory
     * if given file/dir doesn't exist, simply returns
     */
    public static void delete(File file) throws IOException{
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

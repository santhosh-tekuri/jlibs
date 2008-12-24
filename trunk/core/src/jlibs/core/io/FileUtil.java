package jlibs.core.io;

import jlibs.core.lang.ImpossibleException;
import jlibs.core.graph.WalkerUtil;
import jlibs.core.graph.Processor;
import jlibs.core.graph.Path;
import jlibs.core.graph.Walker;
import jlibs.core.graph.walkers.PreorderWalker;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class FileUtil{
    public static final String PATH_SEPARATOR = File.pathSeparator;
    public static final String SEPARATOR = File.separator;
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static @NotNull URL toURL(@NotNull File file){
        try{
            return file.toURI().toURL();
        }catch(MalformedURLException ex){
            throw new ImpossibleException(ex);
        }
    }

    /*-------------------------------------------------[ Delete ]---------------------------------------------------*/
    
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
    
    public static void mkdir(File file) throws IOException{
        if(!file.mkdir())
            throw new IOException("couldn't create directory: "+file);
    }

    public static void mkdirs(File file) throws IOException{
        if(!file.mkdirs())
            throw new IOException("couldn't create directory: "+file);
    }

    public static void copy(File sourceFile, final File targetFile) throws IOException{
        if(sourceFile.isFile())
            IOUtil.pump(new FileInputStream(sourceFile), new FileOutputStream(targetFile), true, true);
        else{
            try{
                mkdirs(targetFile);

                Walker<File> walker = new PreorderWalker<File>(sourceFile, FileNavigator.INSTANCE);
                walker.next();
                WalkerUtil.walk(walker, new Processor<File>(){
                    File target = targetFile;

                    @Override
                    public boolean preProcess(File source, Path path){
                        target = new File(target, source.getName());
                        try{
                            if(source.isDirectory())
                                mkdir(target);
                            else
                                IOUtil.pump(new FileInputStream(source), new FileOutputStream(target), true, true);
                        }catch(IOException ex){
                            throw new RuntimeException(ex);
                        }
                        return true;
                    }

                    @Override
                    public void postProcess(File source, Path path){
                        target = target.getParentFile();
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
        copy(new File("/Users/santhosh/Downloads/xml-schemas"), new File("/Users/santhosh/Downloads/xml-schemas1"));
    }
}

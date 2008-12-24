package jlibs.core.io;

import jlibs.core.lang.StringUtil;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Santhosh Kumar T
 */
public class IOUtil{
    public static URL toURL(String systemID){
        if(StringUtil.isWhitespace(systemID))
            return null;
        systemID = systemID.trim();
        try{
            return new URL(systemID);
        }catch(MalformedURLException ex){
            return FileUtil.toURL(new File(systemID));
        }
    }

    public static void pump(InputStream is, OutputStream os, boolean closeIn, boolean closeOut) throws IOException{
        byte buff[] = new byte[1024];
        int len;
        try{
            while((len=is.read(buff))!=-1)
                os.write(buff, 0, len);
        }finally{
            try{
                if(closeIn)
                    is.close();
            }finally{
                if(closeOut)
                    os.close();
            }
        }
    }

    public static void pump(Reader reader, Writer writer, boolean closeReader, boolean closeWriter) throws IOException{
        char buff[] = new char[1024];
        int len;
        try{
            while((len=reader.read(buff))!=-1)
                writer.write(buff, 0, len);
        }finally{
            try{
                if(closeReader)
                    reader.close();
            }finally{
                if(closeWriter)
                    writer.close();
            }
        }
    }
}

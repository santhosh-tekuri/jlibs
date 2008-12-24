package jlibs.core.io;

import java.io.*;

/**
 * @author Santhosh Kumar T
 */
public class IOUtil{
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

package jlibs.core.io;

import java.io.*;

/**
 * @author Santhosh Kumar T
 */
public class IOUtil{
    public static ByteArrayOutputStream pump(InputStream is, boolean closeIn) throws IOException{
        return pump(is, new ByteArrayOutputStream(), closeIn, true);
    }

    /**
     * Reads data from <code>is</code> and writes it into <code>os</code>.
     * <code>is</code> and <code>os</code> are closed if <code>closeIn</code> and <code>closeOut</code>
     * are true respectively.
     */
    public static <T extends OutputStream> T pump(InputStream is, T os, boolean closeIn, boolean closeOut) throws IOException{
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
        return os;
    }

    public static CharArrayWriter pump(Reader reader, boolean closeReader) throws IOException{
        return pump(reader, new CharArrayWriter(), closeReader, true);
    }

    /**
     * Reads data from <code>reader</code> and writes it into <code>writer</code>.
     * <code>reader</code> and <code>writer</code> are closed if <code>closeReader</code> and <code>closeWriter</code>
     * are true respectively.
     */
    public static <T extends Writer> T pump(Reader reader, T writer, boolean closeReader, boolean closeWriter) throws IOException{
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
        return writer;
    }
}

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

import java.io.*;
import java.nio.charset.Charset;

/**
 * This class contains various Input/Output related utility methods.
 * </p>
 * <br>
 * <b>Stardard Charsets:</b><br>
 * This class contains constants to stardard charsets that are supported by JVM Spec.
 * <pre class="prettyprint">
 * byte buff[] = ...
 * String str = new String(buff, IOUtil.{@link IOUtil#UTF_8 UTF_8});
 * </pre>
 *
 * <b>Pumping:</b><br>
 * To read a file content as String:
 * <pre class="prettyprint">
 * CharArrayWriter cout = new CharArrayWriter();
 * IOUtil.{@link IOUtil#pump(java.io.Reader, java.io.Writer, boolean, boolean) pump}(new FileReader(file), cout, true, true);
 * String content = cout.toString();
 * </pre>
 * To simplify code, <code>pump(...)</code> method returns output; So the above code could be written in single line as follows:
 * <pre class="prettyprint">
 * String content = IOUtil.pump(new FileReader(file), new CharArrayWriter(), true, true).toString();
 * </pre>
 * if output is not specified, it defaults to {@link CharArrayWriter2}. so the same code can be written as:
 * <pre class="prettyprint">
 * String content = IOUtil.{@link IOUtil#pump(java.io.Reader, boolean) pump}(new FileReader(file), true).toString();
 * </pre>
 * Similar versions of pump(...) methods are available for byte-streams also;<br>
 * Let us see how these methods simplify some code;
 * <br><br>
 * To copy file:
 * <pre class="prettyprint">
 * IOUtil.{@link IOUtil#pump(java.io.InputStream, java.io.OutputStream, boolean, boolean) pump}(new FileInputStream(fromFile), new FileOutputStream(toFile), true, true);
 * </pre>
 * To create zip file:
 * <pre class="prettyprint">
 * ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
 * for(File file: files){
 *    zipOut.putNextEntry(new ZipEntry(file.getName());
 *    IOUtil.{@link IOUtil#pump(java.io.InputStream, java.io.OutputStream, boolean, boolean) pump}(new FileInputStream(file), zipOut, true, false); // note that last arg is false
 *    zipOut.closeEntry();
 * }
 * zipOut.close();
 * </pre>
 * To create file with given string:
 * <pre class="prettyprint">
 * String content = ...
 * IOUtil.{@link IOUtil#pump(java.io.Reader, java.io.Writer, boolean, boolean) pump}(new StringReader(content), new FileWriter(file), true, true);
 * </pre>
 * to read a file content into byte array:
 * <pre class="prettyprint">
 * byte bytes[] = IOUtil.{@link IOUtil#pump(java.io.InputStream, boolean) pump}(new FileInputStream(file), true).toByteArray(); // output defaults to {@link ByteArrayOutputStream2}
 * </pre>
 * 
 * @author Santhosh Kumar T
 */
public class IOUtil{
    /*-------------------------------------------------[ Standard Charsets ]---------------------------------------------------*/
    
    public static final Charset US_ASCII   = Charset.forName("US-ASCII");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_8      = Charset.forName("UTF-8");
    public static final Charset UTF_16BE   = Charset.forName("UTF-16BE");
    public static final Charset UTF_16LE   = Charset.forName("UTF-16LE");
    public static final Charset UTF_16     = Charset.forName("UTF-16");

    /*-------------------------------------------------[ Pumping ]---------------------------------------------------*/

    /**
     * Reads data from <code>is</code> and writes it into an instanceof {@link ByteArrayOutputStream2}.<br>
     *
     * @param is        inputstream from which data is read
     * @param closeIn   close inputstream or not
     * @return          the instance of {@link ByteArrayOutputStream2} into which data is written
     * @throws IOException if an I/O error occurs.
     */
    public static ByteArrayOutputStream2 pump(InputStream is, boolean closeIn) throws IOException{
        return pump(is, new ByteArrayOutputStream2(), closeIn, true);
    }

    /**
     * Reads data from <code>is</code> and writes it into <code>os</code>.<br>
     * <code>is</code> and <code>os</code> are closed if <code>closeIn</code> and <code>closeOut</code>
     * are true respectively.
     *
     * @param is        inputstream from which data is read
     * @param os        outputstream into which data is written
     * @param closeIn   close inputstream or not
     * @param closeOut  close outputstream or not
     * @return          the argument <code>os</os>
     * @throws IOException if an I/O error occurs.
     */
    public static <T extends OutputStream> T pump(InputStream is, T os, boolean closeIn, boolean closeOut) throws IOException{
        byte buff[] = new byte[1024];
        int len;
        Exception exception = null;
        try{
            while((len=is.read(buff))!=-1)
                os.write(buff, 0, len);
        }catch(Exception ex){
            exception = ex;
        }finally{
            try{
                try{
                    if(closeIn)
                        is.close();
                }finally{
                    if(closeOut)
                        os.close();
                }
            }catch(IOException ex){
                if(exception!=null)
                    ex.printStackTrace();
                else
                    exception = ex;
            }
        }

        if(exception instanceof IOException)
            throw (IOException)exception;
        else if(exception instanceof RuntimeException)
            throw (RuntimeException)exception;
        return os;
    }

    /**
     * Reads data from <code>reader</code> and writes it into an instanceof {@link CharArrayWriter2}.<br>
     *
     * @param reader        reader from which data is read
     * @param closeReader   close reader or not
     * @return              the instance of {@link CharArrayWriter2} into which data is written
     * @throws IOException if an I/O error occurs.
     */
    public static CharArrayWriter2 pump(Reader reader, boolean closeReader) throws IOException{
        return pump(reader, new CharArrayWriter2(), closeReader, true);
    }

    /**
     * Reads data from <code>reader</code> and writes it into <code>writer</code>.<br>
     * <code>reader</code> and <code>writer</code> are closed if <code>closeReader</code> and <code>closeWriter</code>
     * are true respectively.
     *
     * @param reader        reader from which data is read
     * @param writer        writer into which data is written
     * @param closeReader   close reader or not
     * @param closeWriter   close writer or not
     * @return              the argument <code>writer</os>
     * @throws IOException if an I/O error occurs.
     */
    public static <T extends Writer> T pump(Reader reader, T writer, boolean closeReader, boolean closeWriter) throws IOException{
        char buff[] = new char[1024];
        int len;
        Exception exception = null;
        try{
            while((len=reader.read(buff))!=-1)
                writer.write(buff, 0, len);
        }catch(Exception ex){
            exception = ex;
        }finally{
            try{
                try{
                    if(closeReader)
                        reader.close();
                }finally{
                    if(closeWriter)
                        writer.close();
                }
            }catch(IOException ex){
                if(exception!=null)
                    ex.printStackTrace();
                else
                    exception = ex;
            }
        }

        if(exception instanceof IOException)
            throw (IOException)exception;
        else if(exception instanceof RuntimeException)
            throw (RuntimeException)exception;

        return writer;
    }

    /*-------------------------------------------------[ Read-Fully ]---------------------------------------------------*/

    /**
     * Reads data from given inputstream into specified buffer.<br>
     * If the given inputstream doesn't have number of bytes equal to the length
     * of the buffer available, it simply reads only the available number of bytes.
     *
     * @param in    input stream from which data is read
     * @param b     the buffer into which the data is read.
     * @return      the number of bytes read. if the inputstream doen't have enough bytes available
     *              to fill the buffer, it returns the the number of bytes read
     * @throws IOException if an I/O error occurs.
     */
    public static int readFully(InputStream in, byte b[]) throws IOException {
    	return readFully(in, b, 0, b.length);
    }

    /**
     * Reads <code>len</code> bytes from given input stream into specified buffer.<br>
     * If the given inputstream doesn't have <code>len</code> bytes available,
     * it simply reads only the availabel number of bytes.
     * 
     * @param in    input stream from which data is read
     * @param b     the buffer into which the data is read.
     * @param off   an int specifying the offset into the data.
     * @param len   an int specifying the number of bytes to read.
     * @return      the number of bytes read. if the inputstream doen't have <code>len</code> bytes available
     *              it returns the the number of bytes read
     * @throws IOException if an I/O error occurs.
     */
    public static int readFully(InputStream in, byte b[], int off, int len) throws IOException{
	    if(len<0)
	        throw new IndexOutOfBoundsException();
	    int n = 0;
	    while(n<len){
	        int count = in.read(b, off+n, len-n);
	        if(count<0)
		        return n;
	        n += count;
	    }
        return n;
    }

    /**
     * Reads data from given reader into specified buffer.<br>
     * If the given reader doesn't have number of chars equal to the length
     * of the buffer available, it simply reads only the available number of chars.
     *
     * @param reader    reader from which data is read
     * @param ch        the buffer into which the data is read.
     * @return          the number of chars read. if the reader doen't have enough chars available
     *                  to fill the buffer, it returns the the number of chars read
     * @throws IOException if an I/O error occurs.
     */
    public static int readFully(Reader reader, char ch[]) throws IOException {
    	return readFully(reader, ch, 0, ch.length);
    }

    /**
     * Reads <code>len</code> chars from given reader into specified buffer.<br>
     * If the given reader doesn't have <code>len</code> chars available,
     * it simply reads only the availabel number of chars.
     *
     * @param reader    input stream from which data is read
     * @param ch         the buffer into which the data is read.
     * @param off       an int specifying the offset into the data.
     * @param len       an int specifying the number of chars to read.
     * @return          the number of chars read. if the reader doen't have <code>len</code> chars available
     *                  it returns the the number of chars read
     * @throws IOException if an I/O error occurs.
     */
    public static int readFully(Reader reader, char ch[], int off, int len) throws IOException{
	    if(len<0)
	        throw new IndexOutOfBoundsException();
	    int n = 0;
	    while(n<len){
	        int count = reader.read(ch, off+n, len-n);
	        if(count<0)
		        return n;
	        n += count;
	    }
        return n;
    }

    /**
     * Creates InputStreamReader with detected encoding
     */
    public static InputStreamReader createReader(InputStream is) throws IOException{
        UnicodeInputStream uis = new UnicodeInputStream(is);
        return uis.bom==null ? new InputStreamReader(uis) : new InputStreamReader(uis, uis.bom.encoding());
    }
}

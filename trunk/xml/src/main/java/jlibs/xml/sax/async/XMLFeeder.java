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

package jlibs.xml.sax.async;

import jlibs.core.io.BOM;
import jlibs.core.io.IOUtil;
import jlibs.core.net.URLUtil;
import jlibs.nbp.Feeder;
import jlibs.nbp.NBParser;
import org.apache.xerces.impl.XMLEntityManager;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Locale;

/**
 * @author Santhosh Kumar T
 */
public class XMLFeeder extends Feeder{
    AsyncXMLReader xmlReader;
    String publicID;
    String systemID;
    Runnable postAction;

    public XMLFeeder(AsyncXMLReader xmlReader, NBParser parser, Object source) throws IOException{
        super(parser);
        this.xmlReader = xmlReader;
        init(source);
    }

    void init(Object source) throws IOException{
        publicID = systemID = null;
        postAction = null;
        decoder = DEFAULT_DECODER;
        eofSent = false;
        iProlog = -1;
        child = null;

        if(source instanceof InputSource){
            InputSource is = (InputSource)source;
            publicID = is.getPublicId();
            URL systemURL = null;
            if(is.getSystemId()!=null){
                systemURL = URLUtil.toURL(is.getSystemId());
                systemID = systemURL.toString();
            }

            Reader charStream = is.getCharacterStream();
            if(charStream !=null)
                setSource(charStream);
            else{
                setCharset(is.getEncoding()==null ? IOUtil.UTF_8 : Charset.forName(is.getEncoding()));
                InputStream inputStream = is.getByteStream();
                if(inputStream!=null)
                    setSource(inputStream);
                else{
                    assert systemURL!=null;
                    if(systemURL.getProtocol().equals("file")){
                        try{
                            setSource(new FileInputStream(new File(systemURL.toURI())).getChannel());
                        }catch(URISyntaxException ex){
                            throw new IOException(ex);
                        }
                    }else{
                        // special handling for http url's like redirect, get encoding information from http headers
                        setSource(systemURL.openStream());
                    }
                }
            }
        }else if(source instanceof CharReader){
            this.source = source;
            publicID = ((CharReader)source).feeder.publicID;
            systemID = ((CharReader)source).feeder.systemID;
        }else
            setSource(source);
    }

    @Override
    protected Feeder _feed() throws IOException{
        xmlReader.setFeeder(this);
        if(source instanceof CharReader)
            return feed((CharReader)source);
        else
            return super._feed();
    }

    private Feeder feed(CharReader reader) throws IOException{
        parser.consume(new char[]{ ' '}, 0, 1);
        reader.index = 0;
        if(child!=null)
            return child;

        char chars[] = reader.chars;
        int index = reader.index;
        int len = chars.length;
        reader.index = parser.consume(chars, index, index+len);
        if(child!=null)
            return child;

        parser.consume(new char[]{ ' '}, 0, 1);
        reader.index++;

        // EOF is not sent for CharReader
        return child!=null ? child : parent();
    }

    // == -1 detectBOM
    // <  6  see if it has prolog
    // ==7   found declared encoding
    private int iProlog = -1;
    private static char[] prologStart = { '<', '?', 'x', 'm', 'l', ' ' };
    CharBuffer singleChar = CharBuffer.allocate(1);
    protected void feedByteBuffer(boolean eof) throws IOException{
        if(iProlog==-1){
            if(byteBuffer.remaining()>=4){
                byte marker[] = new byte[]{ byteBuffer.get(0), byteBuffer.get(1), byteBuffer.get(2), byteBuffer.get(3) };
                BOM bom = BOM.get(marker, true);
                if(bom!=null){
                    byteBuffer.position(bom.with().length);
                }else{
                    bom = BOM.get(marker, false);
                    if(bom==null)
                        bom = BOM.UTF8;
                }
                setCharset(Charset.forName(bom.encoding()));
                iProlog = 0;
            }else{
                if(eof)
                    super.feedByteBuffer(true);
                return;
            }
        }
        while(iProlog<6){
            if(eof){
                charBuffer.append("<?xml ", 0, iProlog);
                super.feedByteBuffer(true);
                return;
            }
            singleChar.clear();
            CoderResult coderResult = decoder.decode(byteBuffer, singleChar, eof);
            if(coderResult.isUnderflow() || coderResult.isOverflow()){
                char ch = singleChar.array()[0];
                if(isPrologStart(ch)){
                    iProlog++;
                    if(iProlog==6)
                        parser.consume(prologStart, 0, iProlog);
                    if(coderResult.isUnderflow())
                        return;
                }else{
                    charBuffer.append("<?xml ", 0, iProlog);
                    charBuffer.append(ch);
                    iProlog = 7;
                }
            }else
                parser.encodingError(coderResult);
        }
        if(iProlog==6 && !eof){
            while(iProlog!=7){
                singleChar.clear();
                CoderResult coderResult = decoder.decode(byteBuffer, singleChar, eof);
                if(coderResult.isUnderflow() || coderResult.isOverflow()){
                    parser.consume(singleChar.array(), 0, 1);
                    if(coderResult.isUnderflow())
                        return;
                }else
                    parser.encodingError(coderResult);
            }
        }
        super.feedByteBuffer(eof);
    }


    private boolean isPrologStart(char ch){
        switch(iProlog){
            case 0:
                return ch=='<';
            case 1:
                return ch=='?';
            case 2:
                return ch=='x';
            case 3:
                return ch=='m';
            case 4:
                return ch=='l';
            case 5:
                return ch==0x20 || ch==0x9 || ch==0xa || ch==0xd;
            default:
                throw new Error("impossible");
        }
    }

    void setDeclaredEncoding(String encoding){
        iProlog = 7;
        if(encoding!=null){
            String detectedEncoding = decoder.charset().name().toUpperCase(Locale.ENGLISH);
            String declaredEncoding = encoding.toUpperCase(Locale.ENGLISH);
            if(!detectedEncoding.equals(declaredEncoding)){
                if(detectedEncoding.startsWith("UTF-16") && declaredEncoding.equals("UTF-16"))
                    return;
                if(!detectedEncoding.equals(encoding))
                    setCharset(Charset.forName(encoding));
            }
        }
    }

    public void setCharset(Charset charset){
        super.setCharset(charset);
        if(!decoder.charset().name().equals("UTF-8")){
            decoder.onMalformedInput(CodingErrorAction.REPLACE)
                   .onMalformedInput(CodingErrorAction.REPLACE);
        }
    }

    public InputSource resolve(String publicID, String systemID) throws IOException{
        InputSource inputSource = new InputSource(resolve(systemID));
        inputSource.setPublicId(publicID);
        return inputSource;
    }

    public String resolve(String systemID) throws IOException{
        return XMLEntityManager.expandSystemId(systemID, this.systemID, false);
    }
}

class CharReader{
    XMLFeeder feeder;
    char chars[];
    int index = -1;

    CharReader(XMLFeeder feeder, char[] chars){
        this.feeder = feeder;
        this.chars = chars;
    }
}

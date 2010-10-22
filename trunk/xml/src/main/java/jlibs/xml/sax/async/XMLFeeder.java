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

import jlibs.core.net.URLUtil;
import jlibs.nbp.*;
import org.apache.xerces.impl.XMLEntityManager;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
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

    public XMLFeeder(AsyncXMLReader xmlReader, NBParser parser, InputSource source) throws IOException{
        super(parser);
        this.xmlReader = xmlReader;
        init(source);
    }

    private CharReader charReader;
    public XMLFeeder(AsyncXMLReader xmlReader, NBParser parser, CharReader charReader) throws IOException{
        super(parser);
        this.xmlReader = xmlReader;
        this.charReader = charReader;
        publicID = charReader.feeder.publicID;
        systemID = charReader.feeder.systemID;
    }

    void init(InputSource is) throws IOException{
        publicID = systemID = null;
        postAction = null;
        eofSent = false;
        iProlog = 0;
        child = null;
        charBuffer.clear();

        publicID = is.getPublicId();
        URL systemURL = null;
        if(is.getSystemId()!=null){
            systemURL = URLUtil.toURL(is.getSystemId());
            systemID = systemURL.toString();
        }

        Reader charStream = is.getCharacterStream();
        if(charStream !=null){
            channel = new NBReaderChannel(charStream);
            iProlog = 7;
        }else{
            InputStream inputStream = is.getByteStream();
            if(inputStream==null){
                assert systemURL!=null;
                // special handling for http url's like redirect, get encoding information from http headers
                inputStream = systemURL.openStream();
            }
            NBChannel channel = new NBChannel(new InputStreamChannel(inputStream)){
                @Override
                public void decoder(CharsetDecoder decoder){
                    super.decoder(decoder);
                    if(!decoder.charset().name().equals("UTF-8")){
                        decoder.onMalformedInput(CodingErrorAction.REPLACE)
                               .onMalformedInput(CodingErrorAction.REPLACE);
                    }
                }
            };
            if(is.getEncoding()==null)
                channel.setEncoding("UTF-8", true);
            else
                channel.setEncoding(is.getEncoding(), false);

            this.channel = channel;
        }
    }

    // <  6  see if it has prolog
    // ==7   found declared encoding
    private int iProlog = 0;
    private static char[] prologStart = { '<', '?', 'x', 'm', 'l', ' ' };
    CharBuffer singleChar = CharBuffer.allocate(1);

    @Override
    protected Feeder read() throws IOException{
        xmlReader.setFeeder(this);
        if(charReader!=null){
            parser.consume(new char[]{ ' '}, 0, 1);
            charReader.index = 0;
            if(child!=null)
                return child;

            char chars[] = charReader.chars;
            int index = charReader.index;
            int len = chars.length;
            charReader.index = parser.consume(chars, index, index+len);
            if(child!=null)
                return child;

            parser.consume(new char[]{ ' '}, 0, 1);
            charReader.index++;

            // EOF is not sent for CharReader
            return child!=null ? child : parent();
        }else{
            while(iProlog<6){
                singleChar.clear();
                int read = channel.read(singleChar);
                if(read==0)
                    return this;
                else if(read==-1){
                    charBuffer.append("<?xml ", 0, iProlog);
                    return onPrologEOF();
                }else{
                    char ch = singleChar.get(0);
                    if(isPrologStart(ch)){
                        iProlog++;
                        if(iProlog==6)
                            parser.consume(prologStart, 0, iProlog);
                    }else{
                        charBuffer.append("<?xml ", 0, iProlog);
                        charBuffer.append(ch);
                        iProlog = 7;
                    }
                }
            }
            while(iProlog!=7){
                singleChar.clear();
                int read = channel.read(singleChar);
                if(read==0)
                    return this;
                else if(read==-1)
                    return onPrologEOF();
                else
                    parser.consume(singleChar.array(), 0, 1);
            }

            return super.read();
        }
    }

    private Feeder onPrologEOF() throws IOException{
        if(charBuffer.position()>0){
            charBuffer.flip();
            feedCharBuffer();
            charBuffer.compact();
            if(child!=null)
                return child;
        }

        int read = eofSent ? -1 : 0;
        try{
            if(!eofSent){
                if(canClose()){
                    eofSent = true;
                    parser.eof();
                    if(child!=null)
                        return child;
                }
            }
            return parent();
        }finally{
            try{
                if(child==null){
                    if(canClose())
                        parser.reset();
                    channel.close();
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
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
        if(encoding!=null && channel instanceof NBChannel){
            NBChannel nbChannel = (NBChannel)channel;
            String detectedEncoding = nbChannel.decoder().charset().name().toUpperCase(Locale.ENGLISH);
            String declaredEncoding = encoding.toUpperCase(Locale.ENGLISH);
            if(!detectedEncoding.equals(declaredEncoding)){
                if(detectedEncoding.startsWith("UTF-16") && declaredEncoding.equals("UTF-16"))
                    return;
                if(!detectedEncoding.equals(encoding))
                    nbChannel.decoder(Charset.forName(encoding).newDecoder());
            }
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

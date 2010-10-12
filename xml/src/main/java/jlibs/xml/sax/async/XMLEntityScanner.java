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
import jlibs.core.io.UnicodeInputStream;
import jlibs.core.net.URLUtil;
import org.apache.xerces.impl.XMLEntityManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * @author Santhosh Kumar T
 */
public class XMLEntityScanner extends XMLScanner{
    XMLEntityScanner parent;
    
    public XMLEntityScanner(AsyncXMLReader handler, int startingRule){
        super(handler, startingRule);
        handler.xdeclEnd = false;
    }

    static class CharReader{
        char chars[];
        int index = -1;

        CharReader(char[] chars){
            this.chars = chars;
        }
    }
    
    Deque<CharReader> peStack = new ArrayDeque<CharReader>();
    @Override
    protected void consume(char ch) throws IOException{
        super.consume(ch);
        if(!peStack.isEmpty()){
            CharReader reader = peStack.peek();
            int index = reader.index;
            ch = index==-1 || index==reader.chars.length ? ' ' : reader.chars[index];
            reader.index++;
            if(reader.index==reader.chars.length+1)
                peStack.pop();
            consume(ch);
        }
    }

    protected void consumed(int ch){
        consumed = true;
        int line = location.getLineNumber();
        boolean addToBuffer = location.consume(ch);
        if(addToBuffer && buffer.isBufferring())
            buffer.append(location.getLineNumber()>line ? '\n' : ch);
    }

    @Override
    public void reset(int rule){
        super.reset(rule);
        iProlog = -1;
        if(peStack!=null)
            peStack.clear();
    }

    private int iProlog = -1;
    @Override
    public void write(ByteBuffer in, boolean eof) throws IOException{
        if(iProlog==-1){
            if(in.remaining()>=4){
                byte marker[] = new byte[]{ in.get(0), in.get(1), in.get(2), in.get(3) };
                BOM bom = BOM.get(marker, true);
                String encoding;
                if(bom!=null){
                    in.position(bom.with().length);
                    encoding = bom.encoding();
                }else{
                    bom = BOM.get(marker, false);
                    encoding = bom!=null ? bom.encoding() : "UTF-8";
                }
                handler.encoding = encoding;
                decoder = Charset.forName(encoding).newDecoder();
                if(!encoding.equals("UTF-8")){
                    decoder.onMalformedInput(CodingErrorAction.REPLACE)
                           .onMalformedInput(CodingErrorAction.REPLACE);
                }
                iProlog = 0;
            }else{
                if(eof)
                    super.write(in, true);
                return;
            }
        }
        while(iProlog<6){
            if(eof){
                String str = "<?xml ";
                for(int i=0; i<iProlog; i++)
                    consume(str.charAt(i));
                super.write(in, true);
                return;
            }
            CharBuffer charBuffer = CharBuffer.allocate(1);
            CoderResult coderResult = decoder.decode(in, charBuffer, eof);
            if(coderResult.isUnderflow() || coderResult.isOverflow()){
                char ch = charBuffer.array()[0];
                if(isPrologStart(ch)){
                    iProlog++;
                    if(iProlog==6){
                        consume('<');
                        consume('?');
                        consume('x');
                        consume('m');
                        consume('l');
                        consume(' ');
                    }
                    if(coderResult.isOverflow())
                        continue;
                    else
                        return;
                }else{
                    String str = "<?xml ";
                    for(int i=0; i<iProlog; i++)
                        consume(str.charAt(i));
                    consume(ch);
                    iProlog = 7;
                }
            }else
                encodingError(coderResult);
        }
        if(iProlog==6 && !eof){
            while(!handler.xdeclEnd){
                CharBuffer charBuffer = CharBuffer.allocate(1);
                CoderResult coderResult = decoder.decode(in, charBuffer, eof);
                if(coderResult.isUnderflow() || coderResult.isOverflow()){
                    char ch = charBuffer.array()[0];
                    consume(ch);
                    if(coderResult.isUnderflow())
                        return;
                }else
                    encodingError(coderResult);
            }

            String detectedEncoding = decoder.charset().name().toUpperCase(Locale.ENGLISH);
            String declaredEncoding = handler.encoding.toUpperCase(Locale.ENGLISH);
            if(!detectedEncoding.equals(declaredEncoding)){
                if(detectedEncoding.startsWith("UTF-16") && declaredEncoding.equals("UTF-16"))
                    ; //donothing
                else if(!detectedEncoding.equals(handler.encoding)){
                    decoder = Charset.forName(handler.encoding).newDecoder();
                    if(!handler.encoding.equals("UTF-8")){
                        decoder.onMalformedInput(CodingErrorAction.REPLACE)
                               .onMalformedInput(CodingErrorAction.REPLACE);
                    }
                }
            }
            iProlog = 7;
        }
        super.write(in, eof);
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

    public void parse(ReadableByteChannel channel) throws IOException, SAXException{
        try{
            ByteBuffer buffer = ByteBuffer.allocate(100);
            while(channel.read(buffer)!=-1){
                buffer.flip();
                write(buffer, false);
                buffer.compact();
            }
            buffer.flip();
            write(buffer, true);
        }finally{
            channel.close();
        }
    }

    private void parse(InputStream in) throws IOException, SAXException{
        if(parent==null)
            handler.documentStart();

        ReadableByteChannel channel = null;
        if(in instanceof FileInputStream)
            channel = ((FileInputStream)in).getChannel();

        if(channel==null){
            UnicodeInputStream input = new UnicodeInputStream(in);
            String encoding = input.bom!=null ? input.bom.encoding() : IOUtil.UTF_8.name();
            IOUtil.pump(new InputStreamReader(input, encoding), writer, true, true);
        }else
            parse(channel);
    }

    URL sourceURL;
    public void parse(URL sourceURL) throws IOException, SAXException{
        this.sourceURL = sourceURL;
        
        // special handling for http url's like redirect, get encoding information from http headers
        if(sourceURL.getProtocol().equals("file")){
            try{
                parse(new FileInputStream(new File(sourceURL.toURI())));
            }catch(URISyntaxException ex){
                throw new IOException(ex);
            }
        }else
            parse(sourceURL.openStream());
    }

    public void parse(InputSource input) throws IOException, SAXException{
        if(input.getSystemId()!=null)
            sourceURL = URLUtil.toURL(input.getSystemId());

        Reader charStream = input.getCharacterStream();
        if(charStream !=null){
            if(parent==null)
                handler.documentStart();
            IOUtil.pump(charStream, writer, true, true);
        }else{
            InputStream inputStream = input.getByteStream();
            if(inputStream!=null)
                parse(inputStream);
            else
                parse(sourceURL);
        }
    }

    public InputSource resolve(String publicID, String systemID) throws IOException{
        InputSource inputSource = new InputSource(resolve(systemID));
        inputSource.setPublicId(publicID);
        return inputSource;
    }

    public String resolve(String systemID) throws IOException{
        if(systemID!=null && sourceURL!=null)
            systemID = XMLEntityManager.expandSystemId(systemID, sourceURL.toString(), false);
        return systemID;
    }
}

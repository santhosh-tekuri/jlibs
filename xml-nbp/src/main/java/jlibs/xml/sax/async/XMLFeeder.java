/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.xml.sax.async;

import jlibs.core.nio.InputStreamChannel;
import jlibs.nbp.Feeder;
import jlibs.nbp.NBChannel;
import jlibs.nbp.NBParser;
import jlibs.nbp.NBReaderChannel;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * @author Santhosh Kumar T
 */
public class XMLFeeder extends Feeder{
    AsyncXMLReader xmlReader;
    String publicID;
    String systemID;
    Runnable postAction;

    public XMLFeeder(AsyncXMLReader xmlReader, NBParser parser, InputSource source, XMLScanner declParser) throws IOException{
        super(parser);
        this.xmlReader = xmlReader;
        init(source, declParser);
    }

    public static String toURL(String systemID) throws IOException{
        if(systemID==null)
            return null;

        int ix = systemID.indexOf(':', 0);
        if (ix >= 3 && ix <= 8)
            return systemID;
        else{
            String absPath = new File(systemID).getAbsolutePath();
            char sep = File.separatorChar;
            if(sep!='/')
                absPath = absPath.replace(sep, '/');
            if(absPath.length()>0 && absPath.charAt(0)!='/')
                absPath = "/" + absPath;
            return new URL("file", "", absPath).toString();
        }
    }

    final void init(InputSource is, XMLScanner prologParser) throws IOException{
        postAction = null;
        iProlog = 0;
        this.prologParser = prologParser;
        elemDepth = 0;

        publicID = is.getPublicId();
        systemID = toURL(is.getSystemId());

        Reader charStream = is.getCharacterStream();
        if(charStream !=null)
            setChannel(new NBReaderChannel(charStream));
        else{
            ReadableByteChannel byteChannel = null;
            String encoding = is.getEncoding();

            if(is instanceof ChannelInputSource){
                ChannelInputSource channelInputSource = (ChannelInputSource)is;
                byteChannel = channelInputSource.getChannel();
            }
            if(byteChannel==null){
                InputStream inputStream = is.getByteStream();                
                if(inputStream==null){
                    assert systemID!=null;
                    if(systemID.startsWith("file:/")){
                        try{
                            inputStream = new FileInputStream(new File(new URI(systemID)));
                        }catch(URISyntaxException ex){
                            throw new IOException(ex);
                        }
                    }else{
                        URLConnection con = new URL(systemID).openConnection();
                        if(con instanceof HttpURLConnection){
                            final HttpURLConnection httpCon = (HttpURLConnection)con;

                            // set request properties
                            /*
                            Map<String, String> requestProperties = new HashMap<String, String>();
                            for(Map.Entry<String, String> entry: requestProperties.entrySet())
                                httpCon.setRequestProperty(entry.getKey(), entry.getValue());
                            */

                            // set preference for redirection
                            httpCon.setInstanceFollowRedirects(true);
                        }
                        inputStream = con.getInputStream();

                        String contentType;
                        String charset = null;

                        // content type will be string like "text/xml; charset=UTF-8" or "text/xml"
                        String rawContentType = con.getContentType();
                        // text/xml and application/xml offer only one optional parameter
                        int index = (rawContentType != null) ? rawContentType.indexOf(';') : -1;

                        if(index!=-1){
                            // this should be something like "text/xml"
                            contentType = rawContentType.substring(0, index).trim();

                            // this should be something like "charset=UTF-8", but we want to
                            // strip it down to just "UTF-8"
                            charset = rawContentType.substring(index + 1).trim();
                            if(charset.startsWith("charset=")){
                                // 8 is the length of "charset="
                                charset = charset.substring(8).trim();
                                // strip quotes, if present
                                if((charset.charAt(0)=='"' && charset.charAt(charset.length()-1)=='"')
                                    || (charset.charAt(0)=='\'' && charset.charAt(charset.length()-1)=='\'')){
                                    charset = charset.substring(1, charset.length() - 1);
                                }
                            }
                        }else
                            contentType = rawContentType.trim();

                        String detectedEncoding = null;
                        /**  The encoding of such a resource is determined by:
                            1 external encoding information, if available, otherwise
                                 -- the most common type of external information is the "charset" parameter of a MIME package
                            2 if the media type of the resource is text/xml, application/xml, or matches the conventions text/*+xml or application/*+xml as described in XML Media Types [IETF RFC 3023], the encoding is recognized as specified in XML 1.0, otherwise
                            3 the value of the encoding attribute if one exists, otherwise
                            4 UTF-8.
                         **/
                        if(contentType.equals("text/xml")){
                            if(charset!=null)
                                detectedEncoding = charset;
                            else
                                detectedEncoding = "US-ASCII"; // see RFC2376 or 3023, section 3.1
                        }else if(contentType.equals("application/xml")){
                            if(charset!=null)
                                detectedEncoding = charset;
                        }

                        if(detectedEncoding != null)
                            encoding = detectedEncoding;
                    }
                }
                byteChannel = new InputStreamChannel(inputStream);
            }
            nbChannel.setChannel(byteChannel);
            if(encoding==null)
                nbChannel.setEncoding("UTF-8", true);
            else
                nbChannel.setEncoding(encoding, false);
            setChannel(nbChannel);
        }
    }
    private NBChannel nbChannel = new NBChannel(null);

    // <  6  see if it has prolog
    // ==7   found declared encoding
    private int iProlog = 0;
    CharBuffer singleChar = CharBuffer.allocate(1);
    CharBuffer sixChars = CharBuffer.allocate(6);
    XMLScanner prologParser;
    private static final int MAX_PROLOG_LENGTH = 70;

    @Override
    protected Feeder read() throws IOException{
        xmlReader.setFeeder(this);
        if(prologParser !=null){
            while(iProlog<6){
                sixChars.clear();
                int read = channel.read(sixChars);
                if(read==0)
                    return this;
                else if(read==-1){
                    charBuffer.append("<?xml ", 0, iProlog);
                    return onPrologEOF();
                }else{
                    char chars[] = sixChars.array();
                    for(int i=0; i<read; i++){
                        char ch = chars[i];
                        if(isPrologStart(ch)){
                            iProlog++;
                            if(iProlog==6){
                                charBuffer.append("<?xml ");
                                for(i=0; i<MAX_PROLOG_LENGTH; i++){
                                    singleChar.clear();
                                    read = channel.read(singleChar);
                                    if(read==1){
                                        ch = singleChar.get(0);
                                        charBuffer.append(ch);
                                        if(ch=='>')
                                            break;
                                    }else
                                        break;
                                }
                                if(charBuffer.position()>0){
                                    charBuffer.flip();
                                    charBuffer.position(prologParser.consume(charBuffer.array(), charBuffer.position(), charBuffer.limit(), false));
                                    charBuffer.compact();
                                }
                                if(read==0)
                                    return this;
                                else if(read==-1)
                                    return onPrologEOF();
                                break;
                            }
                        }else{
                            charBuffer.append("<?xml ", 0, iProlog);
                            while(i<read)
                                charBuffer.append(chars[i++]);
                            iProlog = 7;
                            prologParser = null;
                            break;
                        }
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
                    prologParser.consume(singleChar.array(), 0, 1, false);
            }
        }
        return super.read();
    }

    private Feeder onPrologEOF() throws IOException{
        charBuffer.flip();
        channel.close();
        channel = null;
        return super.read();
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
        parser.setLocation(prologParser);
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
        if(systemID==null)
            return null;
        else{
            if(this.systemID==null)
                return toURL(systemID);
            else{
                if(systemID.length()==0)
                    return systemID;
                int ix = systemID.indexOf(':', 0);
                if(ix>=3 && ix<=8)
                    return systemID;
                else{
                    try{
                        return new URI(this.systemID).resolve(new URI(systemID)).toString();
                    }catch(URISyntaxException ex){
                        return systemID;
                    }
                }
            }
        }
    }

    int elemDepth;
}
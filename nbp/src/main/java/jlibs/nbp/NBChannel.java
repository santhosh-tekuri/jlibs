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

package jlibs.nbp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * @author Santhosh Kumar T
 */
public class NBChannel implements ReadableCharChannel{
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private ReadableByteChannel channel;
    private final ByteBuffer byteBuffer;
    public NBChannel(ReadableByteChannel channel, int bufferSize){
        byteBuffer = ByteBuffer.allocate(bufferSize);
        setChannel(channel);        
    }

    public NBChannel(ReadableByteChannel channel){
        this(channel, DEFAULT_BUFFER_SIZE);
    }

    public ReadableByteChannel getChannel(){
        return channel;
    }

    public void setChannel(ReadableByteChannel channel){
        this.channel = channel;
        byteBuffer.clear();
        eofSeen = decode = false;
        fallbackEncoding = Charset.defaultCharset().name();
        encoding = null;
        decoder = null;
    }

    public String fallbackEncoding;
    public String encoding;

    public void setEncoding(String encoding, boolean fallback){
        if(fallback)
            fallbackEncoding = encoding;
        else
            this.encoding = encoding;
    }

    private CharsetDecoder decoder;
    public CharsetDecoder decoder(){
        return decoder;
    }

    public void decoder(CharsetDecoder decoder){
        this.decoder = decoder;
    }

    protected CharsetDecoder createDecoder(ByteBuffer byteBuffer, boolean eof){
        if(byteBuffer.remaining()>=4 || eof){
            byte marker[] = new byte[Math.min(4, byteBuffer.remaining())];
            for(int i=marker.length-1; i>=0; i--)
                marker[i] = byteBuffer.get(i);

            String encoding = this.encoding;

            BOM bom = BOM.get(marker, true);
            if(bom!=null){
                byteBuffer.position(bom.with().length);
                if(encoding==null)
                    encoding = bom.encoding();
            }else if(encoding==null){
                bom = BOM.get(marker, false);
                encoding = bom==null ? fallbackEncoding : bom.encoding();
            }
            return Charset.forName(encoding).newDecoder();
        }else
            return null;
    }

    private boolean eofSeen, decode;

    @Override
    public int read(CharBuffer charBuffer) throws IOException{
        int pos = charBuffer.position();
        while(true){
            if(!decode){
                if(eofSeen)
                    return -1;
                else{
                    int read=channel.read(byteBuffer);
                    if(read==0)
                        break;
                    else if(read<0)
                        eofSeen = true;
                    decode = true;
                    byteBuffer.flip();
                }
            }

            if(decoder==null){
                try{
                    CharsetDecoder decoder = createDecoder(byteBuffer, eofSeen);
                    if(decoder!=null)
                        decoder(decoder);
                    else {
                        decode = false;
                        byteBuffer.position(byteBuffer.limit());
                        byteBuffer.limit(byteBuffer.capacity());
                        continue;
                    }
                }catch(Exception ex){
                    throw new IOException(ex);
                }
            }

            CoderResult cr = decoder.decode(byteBuffer, charBuffer, eofSeen);
            if(cr.isOverflow()) // insufficient space in charBuffer
                break;
            else if(cr.isUnderflow()){ // required more bytes
                if(eofSeen){
                    if(byteBuffer.hasRemaining())
                        break;
                    else{
                        if(charBuffer.position()==pos)
                            return -1;
                        decode = false;
                        break;
                    }
                }else{
                    decode = false;
                    byteBuffer.compact();
                    if(!charBuffer.hasRemaining())
                        break;
                }
            }else
                cr.throwException();
        }
        
        return charBuffer.position()-pos;
    }

    @Override
    public boolean isOpen(){
        return channel!=null;
    }

    @Override
    public void close() throws IOException{
        channel.close();
        channel = null;
    }
}

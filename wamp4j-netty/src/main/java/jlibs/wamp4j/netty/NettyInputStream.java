/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

package jlibs.wamp4j.netty;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyInputStream extends InputStream{
    private ByteBuf buffer;

    public void reset(ByteBuf buffer){
        this.buffer = buffer;
    }

    @Override
    public int available() throws IOException{
        return buffer.readableBytes();
    }

    @Override
    public int read() throws IOException{
        if(!buffer.isReadable()){
            return -1;
        }
        return buffer.readByte() & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        int available = buffer.readableBytes();
        if(available ==0)
            return -1;
        len = Math.min(available, len);
        buffer.readBytes(b, off, len);
        return len;
    }

    @Override
    public boolean markSupported(){
        return true;
    }

    @Override
    public void mark(int readlimit){
        buffer.markReaderIndex();
    }

    @Override
    public void reset(){
        buffer.resetReaderIndex();
    }

    @Override
    public long skip(long n) throws IOException{
        int len = n>Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)n;
        len = Math.min(buffer.readableBytes(), len);
        buffer.skipBytes(len);
        return len;
    }

    @Override
    public void close() throws IOException{
        buffer = null;
    }
}

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
import jlibs.wamp4j.spi.WAMPOutputStream;

import java.io.IOException;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyOutputStream extends WAMPOutputStream{
    final ByteBuf buffer;
    public NettyOutputStream(ByteBuf buffer){
        this.buffer = buffer;
    }

    @Override
    public void release(){
        buffer.release();
    }

    @Override
    public WAMPOutputStream duplicate(){
        ByteBuf duplicate = buffer.duplicate();
        duplicate.retain();
        return new NettyOutputStream(duplicate);
    }

    @Override
    public void write(int b) throws IOException{
        buffer.writeByte(b);
    }

    @Override
    public void write(byte[] b) throws IOException{
        buffer.writeBytes(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException{
        buffer.writeBytes(b, off, len);
    }
}

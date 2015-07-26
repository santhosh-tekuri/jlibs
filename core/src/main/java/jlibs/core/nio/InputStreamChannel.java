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

package jlibs.core.nio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Santhosh Kumar T
 */
public final class InputStreamChannel implements ReadableByteChannel{
    private InputStream is;
    public InputStreamChannel(InputStream is){
        this.is = is;
    }

    private boolean eof;
    public boolean isEOF(){
        return eof;
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException{
        int pos = buffer.position();
        int read = is.read(buffer.array(), buffer.arrayOffset()+pos, buffer.remaining());
        if(read>0)
            buffer.position(pos+read);
        else if(read==-1)
            eof = true;
        return read;
    }

    @Override
    public boolean isOpen(){
        return is!=null;
    }

    @Override
    public void close() throws IOException{
        is.close();
        is = null;
    }
}

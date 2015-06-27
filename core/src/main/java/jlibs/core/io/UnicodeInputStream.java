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

package jlibs.core.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author Santhosh Kumar T
 */
public class UnicodeInputStream extends FilterInputStream{
    private ByteBuffer marker = ByteBuffer.allocate(4);
    public final String encoding;
    public final boolean hasBOM;

    public UnicodeInputStream(InputStream delegate) throws IOException{
        this(delegate, EncodingDetector.DEFAULT);
    }

    public UnicodeInputStream(InputStream delegate, EncodingDetector detector) throws IOException{
        super(delegate);

        int len = IOUtil.readFully(delegate, marker.array());
        marker.limit(len);

        encoding = detector.detect(marker);
        hasBOM = marker.position()>0;
        if(!marker.hasRemaining())
            marker = null;
    }

    @Override
    public int read() throws IOException{
        if(marker!=null){
            int b = marker.get() & 0xFF;
            if(!marker.hasRemaining())
                marker = null;
            return b;
        }else
            return super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        if(marker!=null){
            int read = Math.min(marker.remaining(), len);
            System.arraycopy(marker.array(), marker.position(), b, off, read);
            if(read==marker.remaining())
                marker = null;
            else
                marker.position(marker.position()+read);
            return read;
        }
        return super.read(b, off, len);
    }

    public InputStreamReader createReader(){
        if(encoding==null)
            return new InputStreamReader(this);
        else
            return new InputStreamReader(this, Charset.forName(encoding));
    }
}

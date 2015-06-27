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

import jlibs.core.lang.ByteSequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This is an extension of {@link java.io.ByteArrayOutputStream}.
 * <p/>
 * You can get access to the internal byte buffer using
 * {@link #toByteSequence()}
 *
 * @author Santhosh Kumar T
 */
public class ByteArrayOutputStream2 extends ByteArrayOutputStream{
    public ByteArrayOutputStream2(){}

    public ByteArrayOutputStream2(int size){
        super(size);
    }

    public ByteArrayOutputStream2(InputStream is, int readBuffSize, boolean close) throws IOException{
        readFrom(is, readBuffSize, close);
    }

    /**
     * Returns the input data as {@link ByteSequence}.<br>
     * Note that the internal buffer is not copied.
     */
    public ByteSequence toByteSequence(){
        return new ByteSequence(buf, 0, size());
    }

    public int readFrom(InputStream is, int readBuffSize, boolean close) throws IOException{
        int oldSize = size();
        try{
            while(true){
                int bufAvailable = buf.length-size();
                int readAvailable = is.available();
                if(readAvailable==0)
                    readAvailable = Math.max(readBuffSize, bufAvailable);

                if(bufAvailable<readAvailable){
                    buf = Arrays.copyOf(buf, size()+readAvailable);
                    bufAvailable = readAvailable;
                }
                int read = is.read(buf, size(), bufAvailable);
                if(read==-1)
                    return size()-oldSize;
                else
                    count += read;
            }
        }finally{
            if(close)
                is.close();
        }
    }
}

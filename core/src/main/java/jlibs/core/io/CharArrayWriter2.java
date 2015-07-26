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

package jlibs.core.io;

import jlibs.core.lang.CharArray;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * This is an extension of {@link java.io.CharArrayWriter}.
 * <p/>
 * You can get access to the internal char buffer using
 * {@link #toCharSequence()}
 * 
 * @author Santhosh Kumar T
 */
public class CharArrayWriter2 extends CharArrayWriter{
    public CharArrayWriter2(){}

    public CharArrayWriter2(int initialSize){
        super(initialSize);
    }

    public CharArrayWriter2(Reader reader, int readBuffSize, boolean close) throws IOException{
        super(readBuffSize);
        readFrom(reader, readBuffSize, close);
    }

    /**
     * Returns the input data as {@link CharArray}.<br>
     * Note that the internal buffer is not copied.
     */
    public CharArray toCharSequence(){
        return new CharArray(buf, 0, size());
    }

    public int readFrom(Reader reader, int readBuffSize, boolean close) throws IOException{
        int oldSize = size();
        try{
            while(true){
                int bufAvailable = buf.length-size();
                if(bufAvailable<readBuffSize){
                    buf = Arrays.copyOf(buf, size() + readBuffSize);
                    bufAvailable = readBuffSize;
                }
                int read = reader.read(buf, size(), bufAvailable);
                if(read==-1)
                    return size()-oldSize;
                else
                    count += read;
            }
        }finally{
            if(close)
                reader.close();
        }
    }
}
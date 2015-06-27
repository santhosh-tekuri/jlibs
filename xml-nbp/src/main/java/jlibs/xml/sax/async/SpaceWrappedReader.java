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

import java.io.IOException;
import java.io.Reader;

/**
 * @author Santhosh Kumar T
 */
class SpaceWrappedReader extends Reader{
    private char[] data;
    private int index = -1;

    public SpaceWrappedReader(char[] data){
        this.data = data;
    }

    @Override
    public int read(char[] buf, int off, int len) throws IOException{
        if(index==data.length+1)
            return -1;
        
        int givenOff = off;
        if(index==-1){
            buf[off] = ' ';
            off++;
            len--;
            index++;
        }
        if(len>0 && index<data.length){
            int read = Math.min(data.length-index, len);
            System.arraycopy(data, index, buf, off, read);
            off += read;
            index += read;
            len -= read;
        }
        if(len>0 && index==data.length){
            buf[off] = ' ';
            off++;
            index++;
        }
        return off-givenOff;
    }

    @Override
    public void close() throws IOException{
        index = data.length+1;
    }
}

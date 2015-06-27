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
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * @author Santhosh Kumar T
 */
public class NBReaderChannel implements ReadableCharChannel{
    private Reader reader;

    public NBReaderChannel(Reader reader){
        this.reader = reader;
    }

    @Override
    public int read(CharBuffer buffer) throws IOException{
        int pos = buffer.position();
        int read = reader.read(buffer.array(), buffer.arrayOffset()+pos, buffer.remaining());
        if(read!=-1)
            buffer.position(pos+read);
        return read;
    }

    @Override
    public boolean isOpen(){
        return reader!=null;
    }

    @Override
    public void close() throws IOException{
        reader.close();
        reader = null;
    }
}

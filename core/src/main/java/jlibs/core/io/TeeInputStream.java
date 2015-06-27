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
import java.io.OutputStream;

/**
 * A FilterInputStream implemenation which writes the all the bytes that are read
 * from given inputstream to specifed outputstream. 
 *
 * @author Santhosh Kumar T
 */
public class TeeInputStream extends FilterInputStream{
    private final OutputStream delegate;
    private boolean closeDelegate;

    public TeeInputStream(InputStream in, OutputStream delegate, boolean closeDelegate){
        super(in);
        this.delegate = delegate;
        this.closeDelegate = closeDelegate;
    }

    @Override
    public int read() throws IOException{
        int ch = super.read();
        if(ch!=-1)
            delegate.write(ch);
        return ch;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        len = super.read(b, off, len);
        if(len>0)
            delegate.write(b, off, len);
        return len;
    }

    @Override
    public void close() throws IOException{
        try{
            super.close();
        }finally{
            if(closeDelegate)
                delegate.close();
        }
    }
}

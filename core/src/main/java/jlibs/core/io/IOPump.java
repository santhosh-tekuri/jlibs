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

import jlibs.core.lang.ThrowableTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Santhosh Kumar T
 */
public class IOPump extends ThrowableTask<Void, IOException>{
    private InputStream is;
    private OutputStream os;
    private boolean closeIn;
    private boolean closeOut;

    public IOPump(InputStream is, OutputStream os, boolean closeIn, boolean closeOut){
        super(IOException.class);
        this.is = is;
        this.os = os;
        this.closeIn = closeIn;
        this.closeOut = closeOut;
    }

    @Override
    public Void run() throws IOException{
        IOUtil.pump(is, os, closeIn, closeOut);
        return null;
    }
}

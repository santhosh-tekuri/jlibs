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

package jlibs.xml.sax.async;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;

/**
 * @author Santhosh Kumar T
 */
public class ChannelInputSource extends InputSource{
    public ChannelInputSource(){}

    public ChannelInputSource(String systemId){
        super(systemId);
    }

    public ChannelInputSource(InputStream byteStream){
        super(byteStream);
    }

    public ChannelInputSource(Reader characterStream){
        super(characterStream);
    }

    public ChannelInputSource(ReadableByteChannel channel){
        setChannel(channel);
    }

    private ReadableByteChannel channel;

    public ReadableByteChannel getChannel(){
        return channel;
    }

    public void setChannel(ReadableByteChannel channel){
        this.channel = channel;
    }
}

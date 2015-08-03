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

package jlibs.wamp4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import jlibs.wamp4j.spi.MessageType;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * @author Santhosh Kumar Tekuri
 */
public enum WAMPSerialization{
    json("wamp.2.json", MessageType.text, new ObjectMapper()),
    messagePack("wamp.2.msgpack", MessageType.binary, new ObjectMapper(new MessagePackFactory()));

    private final String protocol;
    private final MessageType messageType;
    private final ObjectMapper mapper;

    WAMPSerialization(String protocol, MessageType messageType, ObjectMapper mapper){
        this.protocol = protocol;
        this.messageType = messageType;
        this.mapper = mapper;
    }

    public String protocol(){
        return protocol;
    }

    public MessageType messageType(){
        return messageType;
    }

    public ObjectMapper mapper(){
        return mapper;
    }
}

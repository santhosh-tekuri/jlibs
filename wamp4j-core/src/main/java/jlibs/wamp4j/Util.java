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

import jlibs.wamp4j.error.UnexpectedException;
import jlibs.wamp4j.error.UnsupportedSerializationException;
import jlibs.wamp4j.error.WAMPException;
import jlibs.wamp4j.spi.WAMPSocket;

import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Util{
    public static <T> T nonNull(T obj, String message){
        if(obj==null)
            throw new NullPointerException(message);
        return obj;
    }

    public static long generateID(Map<Long, ?> map, long lastUsed){
        for (;;) {
            ++lastUsed;
            if(lastUsed>9007199254740992L) // 2^53
                lastUsed = 0L;
            if(!map.containsKey(lastUsed))
                return lastUsed;
        }
    }

    public static String[] subProtocols(WAMPSerialization... serializations){
        String subProtocols[] = new String[serializations.length];
        for(int i=0; i<serializations.length; i++)
            subProtocols[i] = serializations[i].protocol();
        return subProtocols;
    }

    public static WAMPSerialization serialization(WAMPSocket socket, WAMPSerialization... serializations) throws UnsupportedSerializationException{
        for(WAMPSerialization serialization : serializations){
            if(serialization.protocol().equals(socket.subProtocol())){
                return serialization;
            }
        }
        throw new UnsupportedSerializationException(socket.subProtocol());
    }

    public static String toString(String... subProtocols){
        StringBuilder builder = new StringBuilder();
        for(String subProtocol : subProtocols){
            if(builder.length()>0)
                builder.append(',');
            builder.append(subProtocol);
        }
        return builder.toString();
    }
}

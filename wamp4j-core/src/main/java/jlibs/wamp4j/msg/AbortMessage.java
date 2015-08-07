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

package jlibs.wamp4j.msg;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.ErrorCode;
import jlibs.wamp4j.WAMPException;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static jlibs.wamp4j.Util.*;

/**
 * Both the Router and the Client may abort the opening of a WAMP session
 *
 * NOTE: ABORT gets sent only before a Session is established
 * NOTE: ABORT is never replied by a Peer
 *
 * @author Santhosh Kumar Tekuri
 */
public class AbortMessage extends WAMPMessage{
    public static final int ID = 3;

    /**
     * dictionary that allows to provide additional, optional closing information
     */
    public final ObjectNode details;

    /**
     * MUST be an URI
     */
    public final String reason;

    public AbortMessage(ObjectNode details, String reason){
        this.details = details;
        this.reason = nonNull(reason, "null reason");
    }

    public AbortMessage(String message, String reason){
        details = instance.objectNode();
        details.put("message", message);
        this.reason = nonNull(reason, "null reason");
    }

    @Override
    public int getID(){
        return ID;
    }

    @Override
    public ArrayNode toArrayNode(){
        ArrayNode array = instance.arrayNode();
        array.add(idNodes[ID]);
        array.add(objectNode(details));
        array.add(reason);
        return array;
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()!=3)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            return new AbortMessage(objectValue(array, 1), textValue(array, 2));
        }
    };
}

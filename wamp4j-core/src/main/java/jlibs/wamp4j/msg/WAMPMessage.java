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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.ErrorCode;
import jlibs.wamp4j.WAMPException;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class WAMPMessage{
    protected static final IntNode idNodes[] = new IntNode[71];
    static{
        int ids[] = {
                HelloMessage.ID,
                WelcomeMessage.ID,
                AbortMessage.ID,
                GoodbyeMessage.ID,
                ErrorMessage.ID,
                PublishMessage.ID,
                PublishedMessage.ID,
                SubscribeMessage.ID,
                SubscribedMessage.ID,
                UnsubscribeMessage.ID,
                UnsubscribedMessage.ID,
                EventMessage.ID,
                CallMessage.ID,
                ResultMessage.ID,
                RegisterMessage.ID,
                RegisteredMessage.ID,
                UnregisterMessage.ID,
                UnregisteredMessage.ID,
                InvocationMessage.ID,
                YieldMessage.ID
        };
        for(int id: ids)
            idNodes[id] = IntNode.valueOf(id);
    }

    public abstract int getID();
    public abstract ArrayNode toArrayNode();

    @Override
    public String toString(){
        return getClass().getSimpleName()+": "+toArrayNode();
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    private static final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    static ObjectNode objectNode(ObjectNode node){
        return node==null ? objectNode : node;
    }

    static int id(ArrayNode array) throws WAMPException{
        return intValue(array, 0);
    }

    static int intValue(ArrayNode array, int index) throws WAMPException{
        JsonNode node = array.get(index);
        if(!node.isNumber())
            throw new WAMPException(ErrorCode.invalidMessage());
        return node.intValue();
    }

    static long longValue(ArrayNode array, int index) throws WAMPException{
        JsonNode node = array.get(index);
        if(!node.isNumber())
            throw new WAMPException(ErrorCode.invalidMessage());
        return node.longValue();
    }

    static String textValue(ArrayNode array, int index) throws WAMPException{
        JsonNode node = array.get(index);
        if(!node.isTextual())
            throw new WAMPException(ErrorCode.invalidMessage());
        return node.textValue();
    }

    static ArrayNode arrayValue(ArrayNode array, int index) throws WAMPException{
        JsonNode node = array.get(index);
        if(!node.isArray())
            throw new WAMPException(ErrorCode.invalidMessage());
        return (ArrayNode)node;
    }

    static ObjectNode objectValue(ArrayNode array, int index) throws WAMPException{
        JsonNode node = array.get(index);
        if(!node.isObject())
            throw new WAMPException(ErrorCode.invalidMessage());
        return (ObjectNode)node;
    }
}

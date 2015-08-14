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
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.error.InvalidMessageException;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

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
    public abstract void toArrayNode(ArrayNode array);

    @Override
    public String toString(){
        ArrayNode array = instance.arrayNode();
        toArrayNode(array);
        return getClass().getSimpleName()+": "+array;
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    private static final ObjectNode objectNode = instance.objectNode();
    static ObjectNode objectNode(ObjectNode node){
        return node==null ? objectNode : node;
    }

    static NumericNode numericNode(ArrayNode array, int index) throws InvalidMessageException{
        JsonNode node = array.get(index);
        if(!node.isNumber())
            throw new InvalidMessageException();
        return (NumericNode)node;
    }

    static int intValue(ArrayNode array, int index) throws InvalidMessageException{
        return numericNode(array, index).intValue();
    }

    static long longValue(ArrayNode array, int index) throws InvalidMessageException{
        return numericNode(array, index).longValue();
    }

    static int id(ArrayNode array) throws InvalidMessageException{
        return intValue(array, 0);
    }

    static String textValue(ArrayNode array, int index) throws InvalidMessageException{
        JsonNode node = array.get(index);
        if(!node.isTextual())
            throw new InvalidMessageException();
        return node.textValue();
    }

    static ArrayNode arrayValue(ArrayNode array, int index) throws InvalidMessageException{
        JsonNode node = array.get(index);
        if(!node.isArray())
            throw new InvalidMessageException();
        return (ArrayNode)node;
    }

    static ObjectNode objectValue(ArrayNode array, int index) throws InvalidMessageException{
        JsonNode node = array.get(index);
        if(!node.isObject())
            throw new InvalidMessageException();
        return (ObjectNode)node;
    }
}

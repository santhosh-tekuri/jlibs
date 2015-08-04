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
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.ErrorCode;
import jlibs.wamp4j.WAMPException;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static jlibs.wamp4j.Util.*;

/**
 * When a Publisher requests to publish an event to some topic,
 * it sends a PUBLISH message to a Broker
 *
 * @author Santhosh Kumar Tekuri
 */
public class PublishMessage extends WAMPMessage{
    public static final int ID = 16;

    /**
     * is a random, ephemeral ID chosen by the Publisher and used to correlate the Broker's response with the request
     */
    public final long requestID;

    /**
     * dictionary that allows to provide additional publication request details in an extensible way
     */
    public final ObjectNode options;

    /**
     * the topic published to
     */
    public final String topic;

    /**
     * list of application-level event payload elements. The list may be of zero length
     */
    public final ArrayNode arguments;

    /**
     * optional dictionary containing application-level event payload, provided as keyword arguments.
     * The dictionary may be empty
     */
    public final ObjectNode argumentsKw;


    public PublishMessage(long requestID, ObjectNode options, String topic, ArrayNode arguments, ObjectNode argumentsKw){
        this.requestID = requestID;
        this.options = options;
        this.topic = nonNull(topic, "null topic");
        this.arguments = arguments;
        this.argumentsKw = argumentsKw;
    }

    public PublishMessage(long requestID, ObjectNode options, String topic, ArrayNode arguments){
        this(requestID, options, topic, arguments, null);
    }

    public PublishMessage(long requestID, ObjectNode options, String topic){
        this(requestID, options, topic, null, null);
    }


    @Override
    public int getID(){
        return ID;
    }

    @Override
    public ArrayNode toArrayNode(){
        ArrayNode array = instance.arrayNode();
        array.add(ID);
        array.add(requestID);
        array.add(objectNode(options));
        array.add(topic);
        if(arguments!=null)
            array.add(arguments);
        if(argumentsKw!=null)
            array.add(argumentsKw);
        return array;
    }

    public boolean needsAcknowledgement(){
        if(options!=null){
            JsonNode acknowledge = options.get("acknowledge");
            return acknowledge!=null && acknowledge.isBoolean() && acknowledge.booleanValue();
        }
        return false;
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()<4 || array.size()>6)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            long requestID = longValue(array, 1);
            ObjectNode options = objectValue(array, 2);
            String topic = textValue(array, 3);
            ArrayNode arguments = array.size()>=5 ? arrayValue(array, 4) : null;
            ObjectNode argumentsKw = array.size()==6 ? objectValue(array, 5) : null;

            return new PublishMessage(requestID, options, topic, arguments, argumentsKw);
        }
    };
}

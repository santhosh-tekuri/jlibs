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
import jlibs.wamp4j.error.InvalidMessageException;

/**
 * When a publication is successful and a Broker dispatches the event,
 * it determines a list of receivers for the event based on Subscribers
 * for the topic published to and, possibly, other information in the event
 *
 * Note that the Publisher of an event will never receive the published
 * event even if the Publisher is also a Subscriber of the topic published to
 *
 * When a Subscriber is deemed to be a receiver,
 * the Broker sends the Subscriber an EVENT message
 *
 * @author Santhosh Kumar Tekuri
 */
public class EventMessage extends WAMPMessage{
    public static final int ID = 36;

    /**
     *  ID for the subscription under which the Subscriber receives the event
     *  the ID for the subscription originally handed out by the Broker to the Subscriber
     */
    public final long subscriptionID;

    /**
     * the ID of the publication of the published event
     */
    public final long publicationID;

    /**
     * a dictionary that allows the Broker to provide additional event details in a extensible way
     */
    public final ObjectNode details;

    /**
     * application-level event payload that was provided with the original publication request
     */
    public final ArrayNode arguments;

    /**
     * application-level event payload that was provided with the original publication request
     */
    public final ObjectNode argumentsKw;


    public EventMessage(long subscriptionID, long publicationID, ObjectNode details, ArrayNode arguments, ObjectNode argumentsKw){
        this.subscriptionID = subscriptionID;
        this.publicationID = publicationID;
        this.details = details;
        this.arguments = arguments;
        this.argumentsKw = argumentsKw;
    }

    public EventMessage(long subscriptionID, long publicationID, ObjectNode details, ArrayNode arguments){
        this(subscriptionID, publicationID, details, arguments, null);
    }

    public EventMessage(long subscriptionID, long publicationID, ObjectNode details){
        this(subscriptionID, publicationID, details, null, null);
    }


    @Override
    public int getID(){
        return ID;
    }

    @Override
    public void toArrayNode(ArrayNode array){
        array.add(idNodes[ID]);
        array.add(subscriptionID);
        array.add(publicationID);
        array.add(objectNode(details));
        if(arguments!=null)
            array.add(arguments);
        if(argumentsKw!=null)
            array.add(argumentsKw);
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws InvalidMessageException{
            if(array.size()<4 || array.size()>6)
                throw new InvalidMessageException();

            assert id(array)==ID;
            long subscriptionID = longValue(array, 1);
            long publicationID = longValue(array, 2);
            ObjectNode details = objectValue(array, 3);
            ArrayNode arguments = array.size()>=5 ? arrayValue(array, 4) : null;
            ObjectNode argumentsKw = array.size()==6 ? objectValue(array, 5) : null;

            return new EventMessage(subscriptionID, publicationID, details, arguments, argumentsKw);
        }
    };
}

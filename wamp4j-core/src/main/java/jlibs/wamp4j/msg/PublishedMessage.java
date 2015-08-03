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
import jlibs.wamp4j.ErrorCode;
import jlibs.wamp4j.WAMPException;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

/**
 * If the Broker is able to fulfill and allowing the publication,
 * and PUBLISH.Options.acknowledge == true, the Broker replies
 * by sending a PUBLISHED message to the Publisher
 *
 * @author Santhosh Kumar Tekuri
 */
public class PublishedMessage extends WAMPMessage{
    public static final int ID = 17;

    /**
     * the ID from the original PUBLISH request
     */
    public final long requestID;

    /**
     * an ID chosen by the Broker for the publication
     */
    public final long publicationID;

    public PublishedMessage(long requestID, long publicationID){
        this.requestID = requestID;
        this.publicationID = publicationID;
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
        array.add(publicationID);
        return array;
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()!=3)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            return new PublishedMessage(longValue(array, 1), longValue(array, 2));
        }
    };
}

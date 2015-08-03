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
 * If the Dealer is able to fulfill and allowing the registration,
 * it answers by sending a REGISTERED message to the Callee
 *
 * @author Santhosh Kumar Tekuri
 */
public class UnregisterMessage extends RequestMessage{
    public static final int ID = 66;

    /**
     * the ID from the original REGISTER request
     */
    public final long requestID;

    /**
     * the ID for the registration to revoke, originally handed out by the Dealer to the Callee
     */
    public final long registrationID;

    public UnregisterMessage(long requestID, long registrationID){
        this.requestID = requestID;
        this.registrationID = registrationID;
    }

    @Override
    public int getID(){
        return ID;
    }

    @Override
    public long getRequestID(){
        return requestID;
    }

    @Override
    public ArrayNode toArrayNode(){
        ArrayNode array = instance.arrayNode();
        array.add(ID);
        array.add(requestID);
        array.add(registrationID);
        return array;
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()!=3)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            return new UnregisterMessage(longValue(array, 1), longValue(array, 2));
        }
    };
}

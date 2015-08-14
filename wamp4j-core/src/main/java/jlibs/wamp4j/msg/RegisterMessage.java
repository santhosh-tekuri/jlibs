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

import static jlibs.wamp4j.Util.nonNull;

/**
 * A Callee announces the availability of an endpoint implementing
 * a procedure with a Dealer by sending a REGISTER message
 *
 * @author Santhosh Kumar Tekuri
 */
public class RegisterMessage extends RequestMessage{
    public static final int ID = 64;

    /**
     * random, ephemeral ID chosen by the Subscriber and used to correlate the Broker's response with the request
     */
    public final long requestID;

    /**
     * a dictionary that allows to provide additional registration request details in a extensible way
     */
    public final ObjectNode options;

    /**
     * the procedure the Callee wants to register
     */
    public final String procedure;

    public RegisterMessage(long requestID, ObjectNode options, String procedure){
        this.requestID = requestID;
        this.options = options;
        this.procedure = nonNull(procedure, "null procedure");
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
    public void toArrayNode(ArrayNode array){
        array.add(idNodes[ID]);
        array.add(requestID);
        array.add(objectNode(options));
        array.add(procedure);
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws InvalidMessageException{
            if(array.size()!=4)
                throw new InvalidMessageException();

            assert id(array)==ID;
            return new RegisterMessage(longValue(array, 1), objectValue(array, 2), textValue(array, 3));
        }
    };
}

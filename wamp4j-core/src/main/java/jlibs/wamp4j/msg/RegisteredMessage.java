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

/**
 * If the Dealer is able to fulfill and allowing the registration,
 * it answers by sending a REGISTERED message to the Callee
 *
 * @author Santhosh Kumar Tekuri
 */
public class RegisteredMessage extends WAMPMessage{
    public static final int ID = 65;

    /**
     * the ID from the original REGISTER request
     */
    public final long requestID;

    /**
     * an ID chosen by the Dealer for the registration
     */
    public final long registrationID;

    public RegisteredMessage(long requestID, long registrationID){
        this.requestID = requestID;
        this.registrationID = registrationID;
    }

    @Override
    public int getID(){
        return ID;
    }

    @Override
    public void toArrayNode(ArrayNode array){
        array.add(idNodes[ID]);
        array.add(requestID);
        array.add(registrationID);
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()!=3)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            return new RegisteredMessage(longValue(array, 1), longValue(array, 2));
        }
    };
}

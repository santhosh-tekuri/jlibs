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
 * A Router completes the opening of a WAMP session by sending a WELCOME reply message to the Client
 *
 * @author Santhosh Kumar Tekuri
 */
public class WelcomeMessage extends WAMPMessage{
    public static final int ID = 2;

    /**
     * MUST be a randomly generated ID specific to the WAMP session. This applies for the lifetime of the session
     */
    public final long sessionID;

    /**
     * dictionary that allows to provide additional information regarding the open session
     */
    public final ObjectNode details;

    public WelcomeMessage(long sessionID, ObjectNode details){
        this.sessionID = sessionID;
        this.details = nonNull(details, "null details");
    }

    @Override
    public int getID(){
        return ID;
    }

    @Override
    public void toArrayNode(ArrayNode array){
        array.add(idNodes[ID]);
        array.add(sessionID);
        array.add(details);
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws InvalidMessageException{
            if(array.size()!=3)
                throw new InvalidMessageException();

            assert id(array)==ID;
            return new WelcomeMessage(longValue(array, 1), objectValue(array, 2));
        }
    };
}

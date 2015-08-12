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
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.ErrorCode;
import jlibs.wamp4j.WAMPException;

/**
 * If the Dealer is able to fulfill (mediate) the call and it allows the call,
 * it sends a INVOCATION message to the respective Callee implementing the procedure
 *
 * @author Santhosh Kumar Tekuri
 */
public class InvocationMessage extends WAMPMessage{
    public static final int ID = 68;

    /**
     *  a random, ephemeral ID chosen by the Dealer and used to correlate the Callee's response with the request
     */
    public final long requestID;

    /**
     * the registration ID under which the procedure was registered at the Dealer
     */
    public final NumericNode registrationID;

    /**
     * a dictionary that allows to provide additional invocation request details in an extensible way
     */
    public final ObjectNode details;

    /**
     * the original list of positional call arguments as provided by the Caller
     */
    public final ArrayNode arguments;

    /**
     * the original dictionary of keyword call arguments as provided by the Caller
     */
    public final ObjectNode argumentsKw;


    public InvocationMessage(long requestID, NumericNode registrationID, ObjectNode details, ArrayNode arguments, ObjectNode argumentsKw){
        this.requestID = requestID;
        this.registrationID = registrationID;
        this.details = details;
        this.arguments = arguments;
        this.argumentsKw = argumentsKw;
    }

    public InvocationMessage(long requestID, NumericNode registrationID, ObjectNode details, ArrayNode arguments){
        this(requestID, registrationID, details, arguments, null);
    }

    public InvocationMessage(long requestID, NumericNode registrationID, ObjectNode details){
        this(requestID, registrationID, details, null, null);
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
        array.add(objectNode(details));
        if(arguments!=null)
            array.add(arguments);
        if(argumentsKw!=null)
            array.add(argumentsKw);
    }

    public YieldMessage yield(ObjectNode options, ArrayNode arguments, ObjectNode argumentsKw){
        return new YieldMessage(requestID, options, arguments, argumentsKw);
    }

    public ErrorMessage error(ObjectNode details, String error, ArrayNode arguments, ObjectNode argumentsKw){
        return new ErrorMessage(ID, requestID, details, error, arguments, argumentsKw);
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()<4 || array.size()>6)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            long requestID = longValue(array, 1);
            NumericNode registrationID = numericNode(array, 2);
            ObjectNode details = objectValue(array, 3);
            ArrayNode arguments = array.size()>=5 ? arrayValue(array, 4) : null;
            ObjectNode argumentsKw = array.size()==6 ? objectValue(array, 5) : null;

            return new InvocationMessage(requestID, registrationID, details, arguments, argumentsKw);
        }
    };
}

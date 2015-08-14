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
import jlibs.wamp4j.error.ErrorCode;
import jlibs.wamp4j.error.InvalidMessageException;

import static jlibs.wamp4j.Util.nonNull;

/**
 * When the request cannot be fulfilled by the Broker,
 * the Broker sends back an ERROR message to the Client
 *
 * @author Santhosh Kumar Tekuri
 */
public class ErrorMessage extends WAMPMessage{
    public static final int ID = 8;

    public final int requestType;

    /**
     * ID from the original request
     */
    public final long requestID;

    public final ObjectNode details;

    /**
     * URI that gives the error of why the request could not be fulfilled
     */
    public final String error;

    public final ArrayNode arguments;

    public final ObjectNode argumentsKw;


    public ErrorMessage(int requestType, long requestID, ObjectNode details, String error, ArrayNode arguments, ObjectNode argumentsKw){
        this.requestType = requestType;
        this.requestID = requestID;
        this.details = details;
        this.error = nonNull(error, "null error");
        this.arguments = arguments;
        this.argumentsKw = argumentsKw;
    }

    public ErrorMessage(int requestType, long requestID, ObjectNode details, String error, ArrayNode arguments){
        this(requestType, requestID, details, error, arguments, null);
    }

    public ErrorMessage(int requestType, long requestID, ObjectNode details, String error){
        this(requestType, requestID, details, error, null, null);
    }

    public ErrorMessage(int requestType, long requestID, ErrorCode errorCode){
        this(requestType, requestID, null, errorCode.uri, errorCode.arguments, errorCode.argumentsKw);
    }

    @Override
    public int getID(){
        return ID;
    }

    @Override
    public void toArrayNode(ArrayNode array){
        array.add(idNodes[ID]);
        array.add(idNodes[requestType]);
        array.add(requestID);
        array.add(objectNode(details));
        array.add(error);
        if(arguments!=null)
            array.add(arguments);
        if(argumentsKw!=null)
            array.add(argumentsKw);
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws InvalidMessageException{
            if(array.size()<4 || array.size()>7)
                throw new InvalidMessageException();

            assert id(array)==ID;
            int requestType = intValue(array, 1);
            long requestID = longValue(array, 2);
            ObjectNode details = objectValue(array, 3);
            String error = textValue(array, 4);
            ArrayNode arguments = array.size()>=6 ? arrayValue(array, 5) : null;
            ObjectNode argumentsKw = array.size()==7 ? objectValue(array, 6) : null;

            return new ErrorMessage(requestType, requestID, details, error, arguments, argumentsKw);
        }
    };
}

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
import jlibs.wamp4j.ErrorCode;
import jlibs.wamp4j.WAMPException;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

/**
 * The Dealer will then send a RESULT message to the original Caller
 *
 * @author Santhosh Kumar Tekuri
 */
public class ResultMessage extends WAMPMessage{
    public static final int ID = 50;

    /**
     *  the ID from the original call request
     */
    public final long requestID;

    /**
     * a dictionary of additional details
     */
    public final ObjectNode details;

    /**
     * the original list of positional result elements as returned by the Callee
     */
    public final ArrayNode arguments;

    /**
     * the original dictionary of keyword result elements as returned by the Callee
     */
    public final ObjectNode argumentsKw;


    public ResultMessage(long requestID, ObjectNode details, ArrayNode arguments, ObjectNode argumentsKw){
        this.requestID = requestID;
        this.details = details;
        this.arguments = arguments;
        this.argumentsKw = argumentsKw;
    }

    public ResultMessage(long requestID, ObjectNode details, ArrayNode arguments){
        this(requestID, details, arguments, null);
    }

    public ResultMessage(long requestID, ObjectNode details){
        this(requestID, details, null, null);
    }


    @Override
    public int getID(){
        return ID;
    }

    @Override
    public ArrayNode toArrayNode(){
        ArrayNode array = instance.arrayNode();
        array.add(idNodes[ID]);
        array.add(requestID);
        array.add(objectNode(details));
        if(arguments!=null)
            array.add(arguments);
        if(argumentsKw!=null)
            array.add(argumentsKw);
        return array;
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()<3 || array.size()>5)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            long requestID = longValue(array, 1);
            ObjectNode details = objectValue(array, 2);
            ArrayNode arguments = array.size()>=4 ? arrayValue(array, 3) : null;
            ObjectNode argumentsKw = array.size()==5 ? objectValue(array, 4) : null;

            return new ResultMessage(requestID, details, arguments, argumentsKw);
        }
    };
}

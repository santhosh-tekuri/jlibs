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
 * If the Callee is able to successfully process and finish the execution of the call,
 * it answers by sending a YIELD message to the Dealer
 *
 * @author Santhosh Kumar Tekuri
 */
public class YieldMessage extends WAMPMessage{
    public static final int ID = 70;

    /**
     *  the ID from the original invocation request
     */
    public final long requestID;

    /**
     * a dictionary that allows to provide additional options
     */
    public final ObjectNode options;

    /**
     * a list of positional result elements (each of arbitrary type). The list may be of zero length
     */
    public final ArrayNode arguments;

    /**
     * a dictionary of keyword result elements (each of arbitrary type). The dictionary may be empty
     */
    public final ObjectNode argumentsKw;


    public YieldMessage(long requestID, ObjectNode options, ArrayNode arguments, ObjectNode argumentsKw){
        this.requestID = requestID;
        this.options = options;
        this.arguments = arguments;
        this.argumentsKw = argumentsKw;
    }

    public YieldMessage(long requestID, ObjectNode options, ArrayNode arguments){
        this(requestID, options, arguments, null);
    }

    public YieldMessage(long requestID, ObjectNode options){
        this(requestID, options, null, null);
    }


    @Override
    public int getID(){
        return ID;
    }

    @Override
    public void toArrayNode(ArrayNode array){
        array.add(idNodes[ID]);
        array.add(requestID);
        array.add(objectNode(options));
        if(arguments!=null)
            array.add(arguments);
        if(argumentsKw!=null)
            array.add(argumentsKw);
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()<3 || array.size()>5)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            long requestID = longValue(array, 1);
            ObjectNode options = objectValue(array, 2);
            ArrayNode arguments = array.size()>=4 ? arrayValue(array, 3) : null;
            ObjectNode argumentsKw = array.size()==5 ? objectValue(array, 4) : null;

            return new YieldMessage(requestID, options, arguments, argumentsKw);
        }
    };
}

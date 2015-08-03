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
import static jlibs.wamp4j.Util.*;

/**
 * After the underlying transport has been established, opening of a WAMP session
 * is initiated by the Client sending a HELLO message to the Router
 *
 * @author Santhosh Kumar Tekuri
 */
public class HelloMessage extends WAMPMessage{
    public static final int ID = 1;

    /**
     * string identifying the realm this session should attach to
     */
    public final String realm;

    /**
     * dictionary that allows to provide additional opening information
     */
    public ObjectNode details;

    public HelloMessage(String realm, ObjectNode details){
        this.realm = nonNull(realm, "null realm");
        this.details = nonNull(details, "null details");
    }

    @Override
    public int getID(){
        return ID;
    }

    @Override
    public ArrayNode toArrayNode(){
        ArrayNode array = instance.arrayNode();
        array.add(ID);
        array.add(realm);
        array.add(details);
        return array;
    }

    static final Decoder decoder = new Decoder(){
        @Override
        public WAMPMessage decode(ArrayNode array) throws WAMPException{
            if(array.size()!=3)
                throw new WAMPException(ErrorCode.invalidMessage());

            assert id(array)==ID;
            return new HelloMessage(textValue(array, 1), objectValue(array, 2));
        }
    };
}

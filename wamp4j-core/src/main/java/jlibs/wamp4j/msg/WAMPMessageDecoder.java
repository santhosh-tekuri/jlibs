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
 * @author Santhosh Kumar Tekuri
 */
public class WAMPMessageDecoder{
    static Decoder decoders[] = new Decoder[71];
    static{
        decoders[HelloMessage.ID] = HelloMessage.decoder;
        decoders[WelcomeMessage.ID] = WelcomeMessage.decoder;
        decoders[AbortMessage.ID] = AbortMessage.decoder;
        decoders[GoodbyeMessage.ID] = GoodbyeMessage.decoder;
        decoders[ErrorMessage.ID] = ErrorMessage.decoder;
        decoders[PublishMessage.ID] = PublishMessage.decoder;
        decoders[PublishedMessage.ID] = PublishedMessage.decoder;
        decoders[SubscribeMessage.ID] = SubscribeMessage.decoder;
        decoders[SubscribedMessage.ID] = SubscribedMessage.decoder;
        decoders[UnsubscribeMessage.ID] = UnsubscribeMessage.decoder;
        decoders[UnsubscribedMessage.ID] = UnsubscribedMessage.decoder;
        decoders[EventMessage.ID] = EventMessage.decoder;
        decoders[CallMessage.ID] = CallMessage.decoder;
        decoders[ResultMessage.ID] = ResultMessage.decoder;
        decoders[RegisterMessage.ID] = RegisterMessage.decoder;
        decoders[RegisteredMessage.ID] = RegisteredMessage.decoder;
        decoders[UnregisterMessage.ID] = UnregisterMessage.decoder;
        decoders[UnregisteredMessage.ID] = UnregisteredMessage.decoder;
        decoders[InvocationMessage.ID] = InvocationMessage.decoder;
        decoders[YieldMessage.ID] = YieldMessage.decoder;
    }

    public static WAMPMessage decode(ArrayNode array) throws WAMPException{
        int id = WAMPMessage.id(array);
        if(id<0 || id>decoders.length-1 || decoders[id]==null)
            throw new WAMPException(ErrorCode.invalidMessage());
        return decoders[id].decode(array);
    }
}

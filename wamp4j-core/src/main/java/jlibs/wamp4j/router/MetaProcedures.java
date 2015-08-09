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

package jlibs.wamp4j.router;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jlibs.wamp4j.msg.CallMessage;
import jlibs.wamp4j.msg.ResultMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class MetaProcedures{
    private static final Map<String, MetaProcedure> metaProcedures = new HashMap<String, MetaProcedure>();
    static{
        register("wamp.session.count", new MetaProcedure(){
            @Override
            public void reply(Session session, CallMessage call){
                ArrayNode args = JsonNodeFactory.instance.arrayNode();
                args.add(session.realm().sessionCount());
                session.send(new ResultMessage(call.requestID, call.options, args));
            }
        });
        register("wamp.session.list", new MetaProcedure(){
            @Override
            public void reply(Session session, CallMessage call){
                ArrayNode args = JsonNodeFactory.instance.arrayNode();
                args.add(session.realm().sessionIDs());
                session.send(new ResultMessage(call.requestID, call.options, args));
            }
        });
    }

    public static void register(String uri, MetaProcedure procedure){
        metaProcedures.put(uri, procedure);
    }

    public static MetaProcedure get(String uri){
        return metaProcedures.get(uri);
    }
}

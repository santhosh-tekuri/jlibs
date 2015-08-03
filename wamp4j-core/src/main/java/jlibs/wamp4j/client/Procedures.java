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

package jlibs.wamp4j.client;

import jlibs.wamp4j.msg.InvocationMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
class Procedures{
    private WAMPClient client;
    private Map<Long, Procedure> idMap = new HashMap<Long, Procedure>();
    private Map<String, Procedure> uriMap = new HashMap<String, Procedure>();

    public Procedures(WAMPClient client){
        this.client = client;
    }

    public void onRegister(Procedure procedure, long registrationID){
        procedure.registrationID = registrationID;
        idMap.put(registrationID, procedure);
        uriMap.put(procedure.uri, procedure);
        procedure.onRegister(client);
    }

    public void onInvocation(InvocationMessage invocation){
        Procedure procedure = idMap.get(invocation.registrationID);
        procedure.onInvocation(client, invocation);
    }

    public void onUnregister(Procedure procedure){
        idMap.remove(procedure.registrationID);
        uriMap.remove(procedure.uri);
        procedure.registrationID = -1;
        procedure.onUnregister(client);
    }

    public Procedure get(String uri){
        return uriMap.get(uri);
    }
}

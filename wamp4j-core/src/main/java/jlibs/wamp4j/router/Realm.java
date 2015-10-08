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
import jlibs.wamp4j.Util;
import jlibs.wamp4j.spi.WAMPServerEndPoint;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

/**
 * @author Santhosh Kumar Tekuri
 */
class Realm{
    final String name;

    private Map<Long, Session> sessions = new HashMap<Long, Session>();
    private long lastSessionID = -1;

    Map<String, Procedure> procedures = new HashMap<String, Procedure>();
    public final Topics topics;

    public Realm(WAMPServerEndPoint server, String name){
        this.name = name;
        topics = new Topics(server);
    }

    public void addSession(Session session){
        lastSessionID = Util.generateID(sessions, lastSessionID);
        sessions.put(lastSessionID, session);
        session.sessionID = lastSessionID;
    }

    public void removeSession(Session session){
        sessions.remove(session.sessionID);
        session.sessionID = -1;
    }

    public int sessionCount(){
        return sessions.size();
    }

    public ArrayNode sessionIDs(){
        ArrayNode sessionIDs = instance.arrayNode();
        for(Long sessionID : sessions.keySet())
            sessionIDs.add(sessionID);
        return sessionIDs;
    }

    @Override
    public String toString(){
        return name;
    }

    public void close(){
        for(Session session : sessions.values())
            session.close();
        sessions.clear();
    }
}

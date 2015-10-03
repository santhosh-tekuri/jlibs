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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.Util;
import jlibs.wamp4j.spi.WAMPOutputStream;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Topics{
    private Map<String, Topic> uris = new HashMap<String, Topic>();
    private Map<Long, Topic> ids = new HashMap<Long, Topic>();
    private long lastID = -1;

    public long subscribe(Session session, String uri){
        Topic topic = uris.get(uri);
        if(topic==null){
            lastID = Util.generateID(ids, lastID);
            topic = new Topic(uri, lastID);
            uris.put(uri, topic);
            ids.put(lastID, topic);
            topic.sessions.add(session);
            session.subscriptions.put(lastID, topic);
        }
        return topic.subscriptionID;
    }

    public boolean unsubscribe(Session session, long subscriptionID){
        Topic topic = ids.get(subscriptionID);
        if(topic==null)
            return false;
        if(!topic.sessions.remove(session))
            return false;
        if(topic.sessions.isEmpty()){
            uris.remove(topic.uri);
            ids.remove(topic.subscriptionID);
        }
        session.subscriptions.remove(subscriptionID);
        return true;
    }

    private final WAMPOutputStream out[] = new WAMPOutputStream[2];
    private final Session prev[] = new Session[2];
    public void publish(Session publisher, ObjectNode options, JsonParser publish) throws Throwable{
        String uri = publish.nextTextValue();
        Topic topic = uris.get(uri);
        if(topic==null || topic.sessions.isEmpty())
            return;

        try{
            for(Session session : topic.sessions){
                if(session!=publisher){
                    int ordinal = session.serialization.ordinal();
                    if(out[ordinal]==null)
                        out[ordinal] = session.eventMessage(topic.subscriptionID, 0, options, publish);
                    if(prev[ordinal]!=null)
                        prev[ordinal].send(out[ordinal].duplicate());
                    prev[ordinal] = session;
                }
            }
            if(prev[0]!=null)
                prev[0].send(out[0]);
            if(prev[1]!=null)
                prev[1].send(out[1]);
        }finally{
            out[0] = null;
            out[1] = null;
            prev[0] = null;
            prev[1] = null;
        }
    }
}

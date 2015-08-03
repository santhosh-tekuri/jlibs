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

import jlibs.wamp4j.Util;
import jlibs.wamp4j.msg.EventMessage;
import jlibs.wamp4j.msg.PublishMessage;

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
        if(session.sessionID!=-1)
            session.subscriptions.remove(subscriptionID);
        return true;
    }

    public void publish(Session publisher, PublishMessage publish){
        Topic topic = uris.get(publish.topic);
        if(topic==null || topic.sessions.isEmpty())
            return;
        EventMessage event = new EventMessage(topic.subscriptionID, 0, null, publish.arguments, publish.argumentsKw);
        for(Session session : topic.sessions){
            if(session!=publisher)
                session.send(event);
        }
    }
}

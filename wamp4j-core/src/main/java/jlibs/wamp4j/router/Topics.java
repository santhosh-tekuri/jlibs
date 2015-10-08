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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.Debugger;
import jlibs.wamp4j.Util;
import jlibs.wamp4j.WAMPSerialization;
import jlibs.wamp4j.msg.EventMessage;
import jlibs.wamp4j.spi.WAMPOutputStream;
import jlibs.wamp4j.spi.WAMPServerEndPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlibs.wamp4j.Debugger.ROUTER;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Topics{
    private final WAMPServerEndPoint server;
    private final Map<String, Topic> uris = new HashMap<String, Topic>();
    private final Map<Long, Topic> ids = new HashMap<Long, Topic>();
    private Long lastID = -1L;

    public Topics(WAMPServerEndPoint server){
        this.server = server;
    }

    public long subscribe(Session session, String uri){
        Topic topic = uris.get(uri);
        if(topic==null){
            lastID = Util.generateID(ids, lastID);
            topic = new Topic(uri, lastID);
            uris.put(uri, topic);
            ids.put(lastID, topic);
            topic.sessions[session.serialization.ordinal()].add(session);
            session.subscriptions.put(lastID, topic);
        }
        return topic.subscriptionID;
    }

    public boolean unsubscribe(Session session, long subscriptionID){
        Topic topic = ids.get(subscriptionID);
        if(topic==null)
            return false;
        if(!topic.sessions[session.serialization.ordinal()].remove(session))
            return false;
        if(topic.sessions[0].isEmpty() && topic.sessions[1].isEmpty()){
            uris.remove(topic.uri);
            ids.remove(topic.subscriptionID);
        }
        session.subscriptions.remove(subscriptionID);
        return true;
    }

    public void publish(Session publisher, ObjectNode options, JsonParser publish) throws Throwable{
        String uri = publish.nextTextValue();
        Topic topic = uris.get(uri);
        if(topic==null)
            return;

        int jsonSubscribers = topic.sessions[0].size();
        int messagePackSubscribers = topic.sessions[1].size();
        if(publisher.subscriptions.get(topic.subscriptionID)!=null){
            if(publisher.serialization.ordinal()==0)
                --jsonSubscribers;
            else
                --messagePackSubscribers;
        }
        if(jsonSubscribers==0 && messagePackSubscribers==0)
            return;

        if(ROUTER)
            Debugger.temp("<- EventMessage: [%d, %d, %d, %s, ...]", EventMessage.ID, topic.subscriptionID, 0, options);
        if(jsonSubscribers>0 && messagePackSubscribers>0){
            WAMPOutputStream jsonOut = server.createOutputStream();
            WAMPOutputStream messagePackOut = server.createOutputStream();
            try{
                JsonGenerator json = WAMPSerialization.json.mapper().getFactory().createGenerator(jsonOut);
                JsonGenerator messagePack = WAMPSerialization.messagePack.mapper().getFactory().createGenerator(messagePackOut);
                json.writeStartArray();
                messagePack.writeStartArray();
                json.writeNumber(EventMessage.ID);
                messagePack.writeNumber(EventMessage.ID);
                json.writeNumber(topic.subscriptionID);
                messagePack.writeNumber(topic.subscriptionID);
                json.writeNumber(0); // publicationID
                messagePack.writeNumber(0); // publicationID
                if(options==null){
                    json.writeStartObject();
                    messagePack.writeStartObject();
                    json.writeEndObject();
                    messagePack.writeEndObject();
                }else{
                    json.writeTree(options);
                    messagePack.writeTree(options);
                }
                while(publish.nextToken()!=null){
                    json.copyCurrentEvent(publish);
                    messagePack.copyCurrentEvent(publish);
                }
                json.close();
                messagePack.close();
            }catch(Throwable thr){
                jsonOut.release();
                messagePackOut.release();
                throw thr;
            }
            publish(publisher, topic.sessions[0], jsonOut);
            publish(publisher, topic.sessions[1], messagePackOut);
        }else{
            WAMPOutputStream out = server.createOutputStream();
            WAMPSerialization serialization = jsonSubscribers>0 ? WAMPSerialization.json : WAMPSerialization.messagePack;
            try{
                JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
                json.writeStartArray();
                json.writeNumber(EventMessage.ID);
                json.writeNumber(topic.subscriptionID);
                json.writeNumber(0); // publicationID
                if(options==null){
                    json.writeStartObject();
                    json.writeEndObject();
                }else
                    json.writeTree(options);
                while(publish.nextToken()!=null)
                    json.copyCurrentEvent(publish);
                json.close();
            }catch(Throwable thr){
                out.release();
                throw thr;
            }
            publish(publisher, topic.sessions[serialization.ordinal()], out);
        }
    }

    private void publish(Session publisher, List<Session> sessions, WAMPOutputStream out){
        Session prev = null;
        for(Session session : sessions){
            if(session!=publisher){
                if(prev!=null)
                    prev.send(out.duplicate());
                prev = session;
            }
        }
        assert prev!=null;
        prev.send(out);
    }
}

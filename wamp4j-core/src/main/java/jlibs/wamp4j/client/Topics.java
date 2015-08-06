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

import jlibs.wamp4j.msg.EventMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
class Topics{
    private final WAMPClient client;
    private final Map<String, Topic> nameMap = new HashMap<String, Topic>();
    private final Map<Long, Topic> idMap = new HashMap<Long, Topic>();

    public Topics(WAMPClient client){
        this.client = client;
    }

    public Topic get(String name){
        return nameMap.get(name);
    }

    public Topic get(Long subscriptionID){
        return idMap.get(subscriptionID);
    }

    public void onSubscribe(long subscriptionID, Subscription subscription){
        Topic topic = idMap.get(subscriptionID);
        if(topic==null){
            topic = new Topic(client, subscription.topic, subscriptionID);
            nameMap.put(topic.uri, topic);
            idMap.put(subscriptionID, topic);
        }else
            assert topic.uri.equals(subscription.topic);
        topic.onSubscribe(subscription);
    }

    public void onEvent(EventMessage event){
        Topic topic = idMap.get(event.subscriptionID);
        topic.onEvent(event);
    }

    public void onUnsubscribe(Subscription subscription){
        Topic topic = idMap.remove(subscription.subscriptionID);
        nameMap.remove(topic.uri);
        assert topic.size()==1;
        topic.onUnsubscribe(subscription);
    }

    public void unsubscribeAll(){
        for(Topic topic : nameMap.values())
            topic.unsubscribeAll();
        nameMap.clear();
        idMap.clear();
    }
}

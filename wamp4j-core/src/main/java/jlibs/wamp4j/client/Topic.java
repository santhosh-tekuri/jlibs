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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Santhosh Kumar Tekuri
 */
class Topic{
    private final WAMPClient client;
    final String uri;
    final long subscriptionID;

    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<Subscription>();

    public Topic(WAMPClient client, String uri, long subscriptionID){
        this.client = client;
        this.uri = uri;
        this.subscriptionID = subscriptionID;
    }

    public void onSubscribe(Subscription subscription){
        subscription.subscriptionID = subscriptionID;
        subscriptions.add(subscription);
        subscription.onSubscribe(client);
    }

    public void onUnsubscribe(Subscription subscription){
        assert size()>1;
        subscription.subscriptionID = -1;
        subscriptions.remove(subscription);
        subscription.onUnsubscribe(client);
    }

    public int size(){
        return subscriptions.size();
    }

    public void onEvent(EventMessage event){
        for(Subscription subscription : subscriptions){
            subscription.onEvent(event);
        }
    }
}

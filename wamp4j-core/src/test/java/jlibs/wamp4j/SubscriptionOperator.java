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

package jlibs.wamp4j;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.client.Subscription;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.msg.EventMessage;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;

/**
 * @author Santhosh Kumar Tekuri
 */
public class SubscriptionOperator{
    private Subscription subscription;
    private final AtomicReference<Object> atomic = new AtomicReference<Object>();

    public SubscriptionOperator(String topic){
        subscription = new Subscription(topic){
            @Override
            public void onSubscribe(WAMPClient client){
                atomic.set(true);
            }

            @Override
            public void onUnsubscribe(WAMPClient client){
                atomic.set(false);
            }

            @Override
            public void onEvent(EventMessage event){
                onMessage(event);
            }

            @Override
            public void onError(WAMPClient client, WAMPException error){
                atomic.set(error);
            }
        };
    }

    public void onMessage(EventMessage event){}

    private ClientOperator client;
    public void subscribeWith(ObjectNode options, ClientOperator client) throws Throwable{
        atomic.set(null);
        client.client.subscribe(options, subscription);
        assertEquals(Await.getResult(atomic), Boolean.TRUE);
        this.client = client;
    }

    public void unsubscribe() throws Throwable{
        atomic.set(null);
        client.client.unsubscribe(subscription);
        assertEquals(Await.getResult(atomic), Boolean.FALSE);
        client = null;
    }
}

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

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class Subscription implements WAMPListener{
    public final String topic;
    long subscriptionID = -1;

    public Subscription(String topic){
        this.topic = topic;
    }

    public abstract void onSubscribe(WAMPClient client);
    public abstract void onUnsubscribe(WAMPClient client);
    public abstract void onEvent(EventMessage event);
}

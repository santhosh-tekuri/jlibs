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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.msg.EventMessage;
import jlibs.wamp4j.netty.NettyWebSocketClient;
import jlibs.wamp4j.netty.NettyWebSocketServer;
import jlibs.wamp4j.router.WAMPRouter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static org.testng.Assert.assertEquals;

/**
 * @author Santhosh Kumar Tekuri
 */
public class PubSubTest{
    private URI uri = URI.create("ws://localhost:8080/wamp4j");
    private RouterOperator router;
    private ClientOperator jlibsClient1;
    private ClientOperator jlibsClient2;
    private ClientOperator marsClient;

    @BeforeClass(description="starts router and clients")
    public void start() throws Throwable{
        router = new RouterOperator(new WAMPRouter(new NettyWebSocketServer(), uri));
        router.bind();
        jlibsClient1 = new ClientOperator(new WAMPClient(new NettyWebSocketClient(), uri, "jlibs"));
        jlibsClient1.connect();
        jlibsClient2 = new ClientOperator(new WAMPClient(new NettyWebSocketClient(), uri, "jlibs"));
        jlibsClient2.connect();
        marsClient = new ClientOperator(new WAMPClient(new NettyWebSocketClient(), uri, "mars"));
        marsClient.connect();
    }

    @Test
    public void test1() throws Throwable{
        jlibsClient1.publish(null, "t1", null, null);

        final AtomicReference<Object> atomic = new AtomicReference<Object>();
        SubscriptionOperator s1 = new SubscriptionOperator("t1"){
            @Override
            public void onMessage(EventMessage event){
                atomic.set(event);
            }
        };

        ObjectNode options = instance.objectNode().put("option1", "value1");
        s1.subscribeWith(options, jlibsClient2);

        atomic.set(null);
        jlibsClient1.publish(null, "t1", null, null);
        EventMessage em = Await.getResult(atomic);
        assertEquals(em.details, instance.objectNode());
        assertEquals(em.arguments, null);
        assertEquals(em.argumentsKw, null);

        ArrayNode arguments = instance.arrayNode().add("arg");
        atomic.set(null);
        jlibsClient1.publish(options, "t1", arguments, null);
        em = Await.getResult(atomic);
        assertEquals(em.details, options);
        assertEquals(em.arguments, arguments);
        assertEquals(em.argumentsKw, null);

        ObjectNode argumentsKw = instance.objectNode().put("key", "value");
        atomic.set(null);
        jlibsClient1.publish(options, "t1", arguments, argumentsKw);
        em = Await.getResult(atomic);
        assertEquals(em.details, options);
        assertEquals(em.arguments, arguments);
        assertEquals(em.argumentsKw, argumentsKw);

        s1.unsubscribe();
        atomic.set(null);
        jlibsClient1.publish(options, "t1", arguments, argumentsKw);
        Thread.sleep(10 * 1000);
        assertEquals(atomic.get(), null);
    }

    @AfterClass(description="stops clients and router")
    public void stop() throws Throwable{
        jlibsClient1.close();
        jlibsClient2.close();
        marsClient.close();
        router.close();
    }
}

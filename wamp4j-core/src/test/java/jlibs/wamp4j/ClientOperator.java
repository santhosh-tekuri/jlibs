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
import jlibs.wamp4j.client.CallListener;
import jlibs.wamp4j.client.PublishListener;
import jlibs.wamp4j.client.SessionListener;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.msg.ResultMessage;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ClientOperator{
    public final WAMPClient client;
    private final AtomicReference<Object> atomic = new AtomicReference<Object>();

    public ClientOperator(WAMPClient client){
        this.client = client;
    }

    public void connect() throws Throwable{
        atomic.set(null);
        client.connect(new SessionListener(){
            @Override
            public void onOpen(WAMPClient client){
                atomic.set(true);
            }

            @Override
            public void onClose(WAMPClient client){
                atomic.set(false);
            }

            @Override
            public void onWarning(WAMPClient client, Throwable warning){
                atomic.set(warning);
            }

            @Override
            public void onError(WAMPClient client, WAMPException error){
                error.printStackTrace();
                atomic.set(error);
            }
        });
        assertEquals(Await.getResult(atomic), Boolean.TRUE);
    }

    public ResultMessage call(ObjectNode options, String procedure, ArrayNode arguments, ObjectNode argumentsKw) throws Throwable{
        final AtomicReference<Object> atomic = new AtomicReference<Object>();
        client.call(options, procedure, arguments, argumentsKw, new CallListener(){
            @Override
            public void onResult(WAMPClient client, ResultMessage result){
                atomic.set(result);
            }

            @Override
            public void onError(WAMPClient client, WAMPException error){
                atomic.set(error);
            }
        });
        return Await.getResult(atomic);
    }

    public void publish(ObjectNode options, String topic, ArrayNode arguments, ObjectNode argumentsKw) throws Throwable{
        final AtomicReference<Object> atomic = new AtomicReference<Object>();
        PublishListener listener = new PublishListener(){
            @Override
            public void onPublish(WAMPClient client){
                atomic.set(true);
            }

            @Override
            public void onError(WAMPClient client, WAMPException error){
                atomic.set(error);
            }
        };
        atomic.set(null);
        client.publish(options, topic, arguments, argumentsKw, listener);
        assertEquals(Await.getResult(atomic), Boolean.TRUE);
    }

    public void close() throws Throwable{
        atomic.set(null);
        client.close();
        assertClosed();
    }

    public void assertClosed() throws Throwable{
        assertEquals(Await.getResult(atomic), Boolean.FALSE);
    }
}

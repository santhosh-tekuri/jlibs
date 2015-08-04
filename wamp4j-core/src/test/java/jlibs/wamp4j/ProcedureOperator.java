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

import jlibs.wamp4j.client.Procedure;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.msg.InvocationMessage;

import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ProcedureOperator{
    private Procedure procedure;
    private final AtomicReference<Object> atomic = new AtomicReference<Object>();

    public ProcedureOperator(String uri){
        procedure = new Procedure(uri){
            @Override
            public void onRegister(WAMPClient client){
                atomic.set(true);
            }

            @Override
            public void onInvocation(WAMPClient client, InvocationMessage invocation){
                onRequest(client, invocation);
            }

            @Override
            public void onUnregister(WAMPClient client){
                atomic.set(false);
            }

            @Override
            public void onError(WAMPClient client, WAMPException error){
                atomic.set(error);
            }
        };
    }

    protected void onRequest(WAMPClient client, InvocationMessage invocation){
    }

    private ClientOperator client;
    public void registerWith(ClientOperator client) throws Throwable{
        atomic.set(null);
        client.client.register(null, procedure);
        assertEquals(Await.getResult(atomic), Boolean.TRUE);
        this.client = client;
    }

    public void unregister() throws Throwable{
        atomic.set(null);
        client.client.unregister(procedure);
        assertEquals(Await.getResult(atomic), Boolean.FALSE);
        client = null;
    }
}

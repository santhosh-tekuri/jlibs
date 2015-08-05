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
import jlibs.wamp4j.msg.CallMessage;
import jlibs.wamp4j.msg.ErrorMessage;
import jlibs.wamp4j.msg.InvocationMessage;
import jlibs.wamp4j.msg.ResultMessage;
import jlibs.wamp4j.netty.NettyWebSocketClient;
import jlibs.wamp4j.netty.NettyWebSocketServer;
import jlibs.wamp4j.router.WAMPRouter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static org.testng.Assert.assertEquals;

/**
 * @author Santhosh Kumar Tekuri
 */
public class RPCTest{
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

    @Test(description="register and unregister twice from different client under same realm")
    public void test1() throws Throwable{
        ProcedureOperator p1 = new ProcedureOperator("p1");
        p1.registerWith(jlibsClient1);
        p1.unregister();
        p1.registerWith(jlibsClient2);
        p1.unregister();
    }

    @Test(description="registering same uri twice with different clients under same realm")
    public void test2() throws Throwable{
        ProcedureOperator p1 = new ProcedureOperator("p1");
        p1.registerWith(jlibsClient1);
        ProcedureOperator p2 = new ProcedureOperator("p1");
        try{
            p2.registerWith(jlibsClient2);
        }catch(WAMPException ex){
            assertEquals(ex.getErrorCode(), ErrorCode.procedureAlreadyExists("p1"));
        }
        p1.unregister();
    }

    @Test(description="registering same uri twice under different realms")
    public void test3() throws Throwable{
        ProcedureOperator p1 = new ProcedureOperator("p1");
        p1.registerWith(jlibsClient1);
        ProcedureOperator p2 = new ProcedureOperator("p1");
        p2.registerWith(marsClient);
        p2.unregister();
        p1.unregister();
    }

    @Test(description="test echo service")
    public void test4() throws Throwable{
        try{
            jlibsClient1.call(null, "p1", null, null);
            throw new RuntimeException("exception should occur");
        }catch(WAMPException wex){
            assertEquals(wex.getErrorCode(), ErrorCode.noSuchProcedure("p1"));
        }
        ProcedureOperator p1 = new ProcedureOperator("p1"){
            @Override
            protected void onRequest(WAMPClient client, InvocationMessage invocation){
                client.reply(invocation.yield(invocation.details, invocation.arguments, invocation.argumentsKw));
            }
        };
        p1.registerWith(jlibsClient2);

        ResultMessage result = jlibsClient1.call(null, "p1", null, null);
        assertEquals(result.details, instance.objectNode());
        assertEquals(result.arguments, null);
        assertEquals(result.argumentsKw, null);

        ArrayNode arguments = instance.arrayNode().add("arg");
        result = jlibsClient1.call(null, "p1", arguments, null);
        assertEquals(result.details, instance.objectNode());
        assertEquals(result.arguments, arguments);
        assertEquals(result.argumentsKw, null);

        ObjectNode options = instance.objectNode().put("option1", "value1");
        ObjectNode argumentsKw = instance.objectNode().put("key", "value");
        result = jlibsClient1.call(options, "p1", arguments, argumentsKw);
        assertEquals(result.details, options);
        assertEquals(result.arguments, arguments);
        assertEquals(result.argumentsKw, argumentsKw);

        p1.unregister();
        try{
            jlibsClient1.call(null, "p1", null, null);
            throw new RuntimeException("exception should occur");
        }catch(WAMPException wex){
            assertEquals(wex.getErrorCode(), ErrorCode.noSuchProcedure("p1"));
        }
    }

    @Test(description="test rpc error")
    public void test5() throws Throwable{
        ProcedureOperator p1 = new ProcedureOperator("p1"){
            @Override
            protected void onRequest(WAMPClient client, InvocationMessage invocation){
                client.reply(invocation.error(invocation.details, "p1.error", invocation.arguments, invocation.argumentsKw));
            }
        };
        p1.registerWith(jlibsClient2);

        ArrayNode arguments = instance.arrayNode().add("arg");
        ObjectNode options = instance.objectNode().put("option1", "value1");
        ObjectNode argumentsKw = instance.objectNode().put("key", "value");
        try{
            jlibsClient1.call(options, "p1", arguments, argumentsKw);
            throw new RuntimeException("exception should occur");
        }catch(WAMPException wex){
            ErrorMessage error = new ErrorMessage(CallMessage.ID, -1, options, "p1.error", arguments, argumentsKw);
            assertEquals(wex.getErrorCode(), new ErrorCode(error));
        }

        p1.unregister();
        try{
            jlibsClient1.call(null, "p1", null, null);
            throw new RuntimeException("exception should occur");
        }catch(WAMPException ex){
            assertEquals(ex.getErrorCode(), ErrorCode.noSuchProcedure("p1"));
        }
    }

    @Test(description="when caller closed before callee replies")
    public void test6() throws Throwable{
        ProcedureOperator p1 = new ProcedureOperator("p1"){
            @Override
            protected void onRequest(WAMPClient client, InvocationMessage invocation){
                jlibsClient2.client.close();
            }
        };
        p1.registerWith(jlibsClient1);
        try{
            jlibsClient2.call(null, "p1", null, null);
        }catch(WAMPException ex){
            assertEquals(ex.getErrorCode(), ErrorCode.systemShutdown());
        }
        p1.unregister();
        jlibsClient2 = new ClientOperator(new WAMPClient(new NettyWebSocketClient(), uri, "jlibs"));
        jlibsClient2.connect();
    }

    @AfterClass(description="stops clients and router")
    public void stop() throws Throwable{
        jlibsClient1.close();
        jlibsClient2.close();
        marsClient.close();
        router.close();
    }
}

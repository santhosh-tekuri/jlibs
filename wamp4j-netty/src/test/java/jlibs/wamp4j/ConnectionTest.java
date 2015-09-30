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

import jlibs.wamp4j.client.ClientOperator;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.netty.NettyClientEndpoint;
import jlibs.wamp4j.netty.NettyServerEndpoint;
import jlibs.wamp4j.router.RouterOperator;
import jlibs.wamp4j.router.WAMPRouter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;

/**
 * @author Santhosh Kumar Tekuri
 */
public class ConnectionTest{
    @Test
    public void test1() throws Throwable{
        URI uri = URI.create("ws://localhost:8080");
        RouterOperator router = new RouterOperator(new WAMPRouter(new NettyServerEndpoint(), uri));
        router.bind();
        ClientOperator client = new ClientOperator(new WAMPClient(new NettyClientEndpoint(), uri, "jlibs"));
        client.connect();
        client.close();
        router.close();
    }

    @Test
    public void test2() throws Throwable{
        URI uri = URI.create("ws://localhost:8080");
        RouterOperator router = new RouterOperator(new WAMPRouter(new NettyServerEndpoint(), uri));
        router.bind();
        uri = URI.create("ws://localhost:8080/");
        ClientOperator client = new ClientOperator(new WAMPClient(new NettyClientEndpoint(), uri, "jlibs"));
        client.connect();
        client.close();
        router.close();
    }

    @Test
    public void test3() throws Throwable{
        URI uri = URI.create("ws://localhost:8080/");
        RouterOperator router = new RouterOperator(new WAMPRouter(new NettyServerEndpoint(), uri));
        router.bind();
        uri = URI.create("ws://localhost:8080");
        ClientOperator client = new ClientOperator(new WAMPClient(new NettyClientEndpoint(), uri, "jlibs"));
        client.connect();
        client.close();
        router.close();
    }

    @Test
    public void test4() throws Throwable{
        URI uri = URI.create("ws://localhost:8080");
        RouterOperator router = new RouterOperator(new WAMPRouter(new NettyServerEndpoint(), uri));
        router.bind();
        uri = URI.create("ws://localhost:8080/junk");
        ClientOperator client = new ClientOperator(new WAMPClient(new NettyClientEndpoint(), uri, "jlibs"));
        try{
            client.connect();
            Assert.fail("connection expected to fail, but succeeded");
        }catch(Throwable thr){
            // error expected
        }
        router.close();
    }

    @Test
    public void test5() throws Throwable{
        URI uri = URI.create("ws://localhost:8080/wamp4j");
        RouterOperator router = new RouterOperator(new WAMPRouter(new NettyServerEndpoint(), uri));
        router.bind();
        uri = URI.create("ws://localhost:8080/wamp4j");
        ClientOperator client = new ClientOperator(new WAMPClient(new NettyClientEndpoint(), uri, "jlibs"));
        client.connect();
        client.close();
        router.close();
    }

    @Test
    public void test6() throws Throwable{
        URI uri = URI.create("ws://localhost:8080/wamp4j");
        RouterOperator router = new RouterOperator(new WAMPRouter(new NettyServerEndpoint(), uri));
        router.bind();
        uri = URI.create("ws://localhost:8080/junk");
        ClientOperator client = new ClientOperator(new WAMPClient(new NettyClientEndpoint(), uri, "jlibs"));
        try{
            client.connect();
            Assert.fail("connection expected to fail, but succeeded");
        }catch(Throwable thr){
            // error expected
        }
        router.close();
    }
}

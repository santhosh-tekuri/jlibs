package jlibs.wamp4j;

import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.netty.NettyWebSocketClient;
import jlibs.wamp4j.netty.NettyWebSocketServer;
import jlibs.wamp4j.router.WAMPRouter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

/**
 * @author Santhosh Kumar Tekuri
 */
public class RPCTests{
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
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.procedureAlreadyExists("p1"));
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

    @AfterClass(description="stops client and router")
    public void stop() throws Throwable{
        jlibsClient1.close();
        jlibsClient2.close();
        marsClient.close();
        router.close();
    }
}

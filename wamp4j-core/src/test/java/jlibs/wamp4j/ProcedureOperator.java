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
        await().untilAtomic(atomic, notNullValue());
        Object result = atomic.getAndSet(null);
        if(result instanceof Throwable)
            throw (Throwable)result;
        assertEquals(result, Boolean.TRUE);
        this.client = client;
    }

    public void unregister() throws Throwable{
        atomic.set(null);
        client.client.unregister(procedure);
        await().untilAtomic(atomic, notNullValue());
        Object result = atomic.getAndSet(null);
        if(result instanceof Throwable)
            throw (Throwable)result;
        assertEquals(result, Boolean.FALSE);
        client = null;
    }
}

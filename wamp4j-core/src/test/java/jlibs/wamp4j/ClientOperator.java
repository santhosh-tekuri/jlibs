package jlibs.wamp4j;

import jlibs.wamp4j.client.SessionListener;
import jlibs.wamp4j.client.WAMPClient;

import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;
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
                atomic.set(error);
            }
        });
        await().untilAtomic(atomic, notNullValue());
        Object result = atomic.getAndSet(null);
        if(result instanceof Throwable)
            throw (Throwable)result;
        assertEquals(result, Boolean.TRUE);
    }

    public void close() throws Throwable{
        atomic.set(null);
        client.close();
        await().untilAtomic(atomic, notNullValue());
        Object result = atomic.getAndSet(null);
        if(result instanceof Throwable)
            throw (Throwable)result;
        assertEquals(result, Boolean.FALSE);
    }
}

package jlibs.wamp4j;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.client.CallListener;
import jlibs.wamp4j.client.SessionListener;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.msg.ResultMessage;

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
        assertEquals(getResult(), Boolean.TRUE);
    }

    public ResultMessage call(final ObjectNode options, final String procedure, final ArrayNode arguments, final ObjectNode argumentsKw) throws Throwable{
        atomic.set(null);
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
        return getResult();
    }

    public void close() throws Throwable{
        atomic.set(null);
        client.close();
        assertEquals(getResult(), Boolean.FALSE);
    }

    private <T> T getResult() throws Throwable{
        await().untilAtomic(atomic, notNullValue());
        Object result = atomic.getAndSet(null);
        if(result instanceof Throwable)
            throw (Throwable)result;
        return (T)result;
    }
}

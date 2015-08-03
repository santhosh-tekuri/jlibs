package jlibs.wamp4j;

import jlibs.wamp4j.router.RouterListener;
import jlibs.wamp4j.router.WAMPRouter;

import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;

/**
 * @author Santhosh Kumar Tekuri
 */
public class RouterOperator{
    public final WAMPRouter router;
    private final AtomicReference<Object> atomic = new AtomicReference<Object>();

    public RouterOperator(WAMPRouter router){
        this.router = router;
    }

    public void bind() throws Throwable{
        atomic.set(null);
        router.bind(new RouterListener(){
            @Override
            public void onBind(WAMPRouter router){
                atomic.set(true);
            }

            @Override
            public void onError(WAMPRouter router, Throwable error){
                atomic.set(error);
            }

            @Override
            public void onClose(WAMPRouter router){
                atomic.set(false);
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
        router.close();
        await().untilAtomic(atomic, notNullValue());
        Object result = atomic.getAndSet(null);
        if(result instanceof Throwable)
            throw (Throwable)result;
        assertEquals(result, Boolean.FALSE);
    }
}

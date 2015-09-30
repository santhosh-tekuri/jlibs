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

package jlibs.wamp4j.router;

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
            public void onWarning(WAMPRouter router, Throwable error){
                error.printStackTrace();
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

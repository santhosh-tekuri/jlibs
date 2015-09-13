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

package jlibs.examples.wamp4j;

import jlibs.wamp4j.netty.NettyServerEndpoint;
import jlibs.wamp4j.router.RouterListener;
import jlibs.wamp4j.router.WAMPRouter;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Router{
    public static void main(String[] args) throws Exception{
        if(args.length!=2){
            System.err.println("arguments: <url> <interval>");
            System.exit(1);
        }
        URI uri = URI.create(args[0]);
        long interval = Long.parseLong(args[1]);
        WAMPRouter router = new WAMPRouter(new NettyServerEndpoint(), uri);
        final CountDownLatch latch = new CountDownLatch(1);
        router.bind(new RouterListener(){
            @Override
            public void onBind(WAMPRouter router){
                System.out.println("RouterListener.onBind");
                latch.countDown();
            }

            @Override
            public void onError(WAMPRouter router, Throwable error){
                System.out.println("RouterListener.onError");
                error.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onWarning(WAMPRouter router, Throwable error){
                System.out.println("RouterListener.onWarning");
                error.printStackTrace();
                latch.countDown();
            }

            @Override
            public void onClose(WAMPRouter router){
                System.out.println("RouterListener.onClose");
            }
        });

        latch.await();
        Runtime runtime = Runtime.getRuntime();
        System.out.printf("%n%5s %6s%n", "Time", "Memory");
        long prev = System.nanoTime();
        long begin = prev;
        Thread.sleep(interval);
        while(true){
            long cur = System.nanoTime();

            double usedMemory = (runtime.totalMemory()- runtime.freeMemory())/(1024*1024.0);

            long nano = cur-begin;
            long sec = TimeUnit.NANOSECONDS.toSeconds(nano);
            nano = nano-TimeUnit.SECONDS.toNanos(sec);
            long min = TimeUnit.SECONDS.toMinutes(sec);
            sec = sec-TimeUnit.MINUTES.toSeconds(min);

            System.out.printf("\r%02d:%02d %6.2f", min, sec, usedMemory);
            prev = cur;
            Thread.sleep(interval);
        }
    }
}


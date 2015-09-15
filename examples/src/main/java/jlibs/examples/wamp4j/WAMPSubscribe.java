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

import jlibs.wamp4j.client.Subscription;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.error.WAMPException;
import jlibs.wamp4j.msg.EventMessage;
import jlibs.wamp4j.netty.NettyClientEndpoint;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Santhosh Kumar Tekuri
 */
public class WAMPSubscribe{
    public static void main(String[] args) throws Exception{
        if(args.length!=4){
            System.err.println("arguments: <uri> <realm> <topic> <interval>");
            System.exit(1);
        }
        URI uri = URI.create(args[0]);
        String realm = args[1];
        final String topic = args[2];
        long interval = Long.parseLong(args[3]);
        WAMPClient client = new WAMPClient(new NettyClientEndpoint(), uri, realm);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicLong events = new AtomicLong();
        client.connect(new SessionAdapter(){
            @Override
            public void onOpen(WAMPClient client){
                client.subscribe(null, new Subscription(topic){
                    @Override
                    public void onSubscribe(WAMPClient client){
                        System.out.println("Subscription.onSubscribe");
                        latch.countDown();
                    }

                    @Override
                    public void onEvent(EventMessage event){
                        events.incrementAndGet();
                    }

                    @Override
                    public void onUnsubscribe(WAMPClient client){
                        System.out.println("Subscription.onUnsubscribe");
                    }

                    @Override
                    public void onError(WAMPClient client, WAMPException error){
                        System.out.println("Subscription.onError");
                        error.printStackTrace();
                        latch.countDown();
                    }
                });
            }
        });

        latch.await();
        Runtime runtime = Runtime.getRuntime();
        System.out.printf("%5s %8s %10s %6s%n", "Time", "Events", "Throughput", "Memory");
        long prev = System.nanoTime();
        long begin = prev;
        Thread.sleep(interval);
        while(true){
            long cur = System.nanoTime();
            long recvd = events.getAndSet(0);

            double usedMemory = (runtime.totalMemory()- runtime.freeMemory())/(1024*1024.0);

            long nano = cur-begin;
            long sec = TimeUnit.NANOSECONDS.toSeconds(nano);
            nano = nano-TimeUnit.SECONDS.toNanos(sec);
            long min = TimeUnit.SECONDS.toMinutes(sec);
            sec = sec-TimeUnit.MINUTES.toSeconds(min);

            double duration = ((double)(cur-prev))/ TimeUnit.SECONDS.toNanos(1);
            double throughput = (double)(recvd)/duration;
            System.out.printf("\r%02d:%02d %8d %10.2f %6.2f", min, sec, recvd, throughput, usedMemory);
            prev = cur;
            Thread.sleep(interval);
        }
    }
}

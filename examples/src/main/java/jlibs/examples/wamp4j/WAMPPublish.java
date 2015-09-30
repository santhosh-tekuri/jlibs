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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jlibs.wamp4j.client.CallListener;
import jlibs.wamp4j.client.PublishListener;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.error.WAMPException;
import jlibs.wamp4j.msg.ResultMessage;
import jlibs.wamp4j.netty.NettyClientEndpoint;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Santhosh Kumar Tekuri
 */
public class WAMPPublish{
    public static void main(String[] args) throws Exception{
        if(args.length!=6){
            System.err.println("arguments: <uri> <realm> <topic> <blocking> <clients> <interval>");
            System.exit(1);
        }
        URI uri = URI.create(args[0]);
        String realm = args[1];
        final String topic = args[2];
        final boolean blocking = Boolean.valueOf(args[3]);
        int clients = Integer.parseInt(args[4]);
        long interval = Long.parseLong(args[5]);

        final CountDownLatch latch = new CountDownLatch(1+clients);
        final PublishThread threads[] = new PublishThread[clients];
        for(int i=0; i<threads.length; i++){
            final int index = i;
            new WAMPClient(new NettyClientEndpoint(), uri, realm).connect(new SessionAdapter(){
                @Override
                public void onOpen(WAMPClient client){
                    System.out.println("client"+index+" connected to wamp-router");
                    PublishThread thread;
                    if(blocking)
                        thread = new BlockingPublishThread(client, topic, latch);
                    else
                        thread = new NonBlockingPublishThread(client, topic, latch);
                    threads[index] = thread;
                    thread.start();
                }
            });
        }

        latch.countDown();
        latch.await();

        Runtime runtime = Runtime.getRuntime();

        System.out.printf("%5s %8s %7s %7s %10s %6s %7s %n", "Time", "Requests", "Replies", "Errors", "Throughput", "Memory", "Pending");
        long prev = System.nanoTime();
        long begin = prev;
        Thread.sleep(interval);
        long totalRequests = 0;
        long totalReplies = 0;
        while(true){
            long cur = System.nanoTime();
            long requests = 0;
            long replies = 0;
            long errors = 0;
            for(PublishThread thread : threads){
                requests += thread.requests.getAndSet(0);
                replies += thread.replies.getAndSet(0);
                errors += thread.errors.getAndSet(0);
            }
            totalRequests += requests;
            totalReplies += replies;
            double usedMemory = (runtime.totalMemory()- runtime.freeMemory())/(1024*1024.0);

            long nano = cur-begin;
            long sec = TimeUnit.NANOSECONDS.toSeconds(nano);
            nano = nano-TimeUnit.SECONDS.toNanos(sec);
            long min = TimeUnit.SECONDS.toMinutes(sec);
            sec = sec-TimeUnit.MINUTES.toSeconds(min);

            double duration = ((double)(cur-prev))/ TimeUnit.SECONDS.toNanos(1);
            double throughput = (double)(replies+errors)/duration;
            System.out.printf("\r%02d:%02d %8d %7d %7d %10.2f %6.2f %7d", min, sec, requests, replies, errors, throughput, usedMemory, (totalRequests-totalReplies));
            prev = cur;
            Thread.sleep(interval);
        }
    }
}


abstract class PublishThread extends Thread{
    private static int count = 0;

    final WAMPClient client;
    final String topic;
    final CountDownLatch latch;
    final AtomicLong requests = new AtomicLong();
    final AtomicLong replies = new AtomicLong();
    final AtomicLong latencies = new AtomicLong();
    final AtomicLong errors = new AtomicLong();

    public PublishThread(WAMPClient client, String topic, CountDownLatch latch){
        super("PublishThread"+count++);
        this.client = client;
        this.topic = topic;
        this.latch = latch;
    }

    public void run(){
        latch.countDown();
        try{
            latch.await();
            doRun();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    protected abstract void doRun();
}

class BlockingPublishListener implements PublishListener{
    public WAMPException error;

    @Override
    public void onPublish(WAMPClient client){
        synchronized(this){
            notifyAll();
        }
    }

    @Override
    public void onError(WAMPClient client, WAMPException error){
        synchronized(this){
            this.error = error;
            notifyAll();
        }
    }
}

class BlockingPublishThread extends PublishThread{
    public BlockingPublishThread(WAMPClient client, String topic, CountDownLatch latch){
        super(client, topic, latch);
    }

    private final BlockingPublishListener listener = new BlockingPublishListener();

    @Override
    protected void doRun(){
        long requests = 0;
        long replies = 0;
        long errors = 0;
        while(!Thread.interrupted()){
            ++requests;
            try{
                long begin = System.nanoTime();
                listener.error = null;
                synchronized(listener){
                    client.publish(null, topic, null, null, listener);
                    listener.wait();
                }
                if(listener.error!=null)
                    throw listener.error;
                latencies.addAndGet(System.nanoTime()-begin);
                ++replies;
            }catch(Throwable throwable){
                if(throwable instanceof InterruptedException)
                    break;
                ++errors;
                throwable.printStackTrace();
            }
        }
        this.requests.set(requests);
        this.replies.set(replies);
        this.errors.set(errors);
    }
}

class NonBlockingPublishThread extends PublishThread implements PublishListener{
    public NonBlockingPublishThread(WAMPClient client, String procedure, CountDownLatch latch){
        super(client, procedure, latch);
    }

    @Override
    protected void doRun(){
        while(!Thread.interrupted()){
            client.publish(null, topic, null, null, this);
            requests.incrementAndGet();
        }
    }

    @Override
    public void onPublish(WAMPClient client){
        replies.incrementAndGet();
    }

    @Override
    public void onError(WAMPClient client, WAMPException error){
        errors.incrementAndGet();
        error.printStackTrace();
    }
}
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

import jlibs.wamp4j.client.CallListener;
import jlibs.wamp4j.client.Procedure;
import jlibs.wamp4j.client.SessionListener;
import jlibs.wamp4j.client.WAMPClient;
import jlibs.wamp4j.error.WAMPException;
import jlibs.wamp4j.msg.InvocationMessage;
import jlibs.wamp4j.msg.ResultMessage;
import jlibs.wamp4j.netty.NettyClientEndpoint;
import jlibs.wamp4j.netty.NettyServerEndpoint;
import jlibs.wamp4j.router.RouterListener;
import jlibs.wamp4j.router.WAMPRouter;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Benchmark{
    private static final String realm = "my-realm";
    private static final URI uri = URI.create("ws://localhost:8080/test");
    public static final String HELLO_WORLD = "hello_world";

    private static abstract class RPCThread extends Thread{
        final WAMPClient client;
        final CountDownLatch latch;
        final AtomicLong requests = new AtomicLong();
        final AtomicLong replies = new AtomicLong();
        final AtomicLong errors = new AtomicLong();

        public RPCThread(WAMPClient client, CountDownLatch latch){
            this.client = client;
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

    private static class BlockingRPCThread extends RPCThread{
        public BlockingRPCThread(WAMPClient client, CountDownLatch latch){
            super(client, latch);
        }

        @Override
        protected void doRun(){
            while(!Thread.interrupted()){
                requests.incrementAndGet();
                try{
                    client.call(null, HELLO_WORLD, null, null);
                    replies.incrementAndGet();
                }catch(Throwable throwable){
                    errors.incrementAndGet();
                    if(throwable instanceof InterruptedException)
                        break;
                    throwable.printStackTrace();
                }
            }
        }
    }

    private static class NonBlockingRPCThread extends RPCThread implements CallListener{
        public NonBlockingRPCThread(WAMPClient client, CountDownLatch latch){
            super(client, latch);
        }

        @Override
        protected void doRun(){
            while(!Thread.interrupted()){
                requests.incrementAndGet();
                client.call(null, HELLO_WORLD, null, null, this);
            }
        }

        @Override
        public void onResult(WAMPClient client, ResultMessage result){
            replies.incrementAndGet();
        }

        @Override
        public void onError(WAMPClient client, WAMPException error){
            errors.incrementAndGet();
            error.printStackTrace();
        }
    }

    static abstract class SessionAdapter implements SessionListener{
        @Override
        public void onClose(WAMPClient client){
            System.out.println("SessionAdapter.onClose");
        }

        @Override
        public void onWarning(WAMPClient client, Throwable warning){
            System.out.println("SessionAdapter.onWarning");
            warning.printStackTrace();
        }

        @Override
        public void onError(WAMPClient client, WAMPException error){
            System.out.println("SessionAdapter.onError");
            error.printStackTrace();
        }
    }

    static abstract class ProcedureAdapter extends Procedure{
        public ProcedureAdapter(String uri){
            super(uri);
        }

        @Override
        public void onRegister(WAMPClient client){
            System.out.println("ProcedureAdapter.onRegister");
        }

        @Override
        public void onUnregister(WAMPClient client){
            System.out.println("ProcedureAdapter.onUnregister");
        }

        @Override
        public void onError(WAMPClient client, WAMPException error){
            System.out.println("ProcedureAdapter.onError");
            error.printStackTrace();
        }
    }

    static class Router{
        public static void main(String[] args){
            WAMPRouter router = new WAMPRouter(new NettyServerEndpoint(), uri);
            router.bind(new RouterListener(){
                @Override
                public void onBind(WAMPRouter router){
                    System.out.println("RouterListener.onBind");
                }

                @Override
                public void onError(WAMPRouter router, Throwable error){
                    System.out.println("RouterListener.onError");
                    error.printStackTrace();
                }

                @Override
                public void onClose(WAMPRouter router){
                    System.out.println("RouterListener.onClose");
                }
            });
        }
    }

    static class Client{
        public static void main(String[] args) throws Exception{
            WAMPClient client = new WAMPClient(new NettyClientEndpoint(), uri, realm);
            client.connect(new SessionAdapter(){
                @Override
                public void onOpen(WAMPClient client){
                    client.register(null, new ProcedureAdapter(HELLO_WORLD){
                        @Override
                        public void onInvocation(WAMPClient client, InvocationMessage invocation){
                            client.reply(invocation.yield(null, null, null));
                        }
                    });
                }
            });
        }
    }

    private static final long nanos = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
    public static void main(String[] args) throws Exception{
        if(args.length==0){
            System.err.println("needs arguments: {blocking} {threads} {duration}");
            return;
        }
        final boolean blocking = Boolean.parseBoolean(args[0]);
        final int threadCount = Integer.parseInt(args[1]);
        final long duration = Long.parseLong(args[2]);
        System.out.printf("configuration: { java_version: %s, blocking: %s, threads: %d, duration: %d}%n", System.getProperty("java.version"), blocking, threadCount, duration);

        final CountDownLatch latch = new CountDownLatch(1+threadCount);
        final RPCThread threads[] = new RPCThread[threadCount];
        for(int i=0; i<threads.length; i++){
            final int index = i;
            new WAMPClient(new NettyClientEndpoint(), uri, realm).connect(new SessionAdapter(){
                @Override
                public void onOpen(WAMPClient client){
                    System.out.println("client"+index+" connected to wamp-router");
                    RPCThread thread;
                    if(blocking)
                        thread = new BlockingRPCThread(client, latch);
                    else
                        thread = new NonBlockingRPCThread(client, latch);
                    threads[index] = thread;
                    thread.start();
                }
            });
        }

        latch.countDown();
        latch.await();
        long begin = System.nanoTime();
        Thread.sleep(duration);
        System.out.println("interrupting");
        for(RPCThread thread : threads)
            thread.interrupt();
        System.out.println("waiting to join");
        for(RPCThread thread : threads)
            thread.join();

        long requests = 0;
        for(RPCThread thread : threads){
            requests += thread.requests.get();
        }

        while(true){
            long replies = 0;
            for(RPCThread thread : threads){
                replies += thread.replies.get();
            }

            long errors = 0;
            for(RPCThread thread : threads){
                errors += thread.errors.get();
            }

            long end = System.nanoTime();
            double seconds = ((double)(end-begin))/nanos;
            System.out.println(" ------------------------------- "+seconds);
            System.out.println("  requests: "+requests);
            System.out.println("   replies: "+replies);
            System.out.println("   errors: "+errors);
            double throughput = (double)(replies+errors)/seconds;
            System.out.println("throughput: " + throughput+"/sec");
            if(requests==replies)
                break;
            Thread.sleep(10*1000);
        }
        System.exit(0);
    }
}

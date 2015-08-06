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
import jlibs.wamp4j.msg.InvocationMessage;
import jlibs.wamp4j.msg.ResultMessage;
import jlibs.wamp4j.netty.NettyWebSocketClient;
import jlibs.wamp4j.netty.NettyWebSocketServer;
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

    private static class Blocking implements Runnable{
        private WAMPClient client;
        private RPCThread threads[];
        private CountDownLatch latch;
        private long duration;
        public Blocking(WAMPClient client, int threadCount, long duration){
            this.client = client;
            threads = new RPCThread[threadCount];
            latch = new CountDownLatch(threadCount+1);
            this.duration = duration;
        }

        @Override
        public void run(){
            for(int i=0; i<threads.length; i++)
                (threads[i]=new RPCThread()).start();
            try{
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
                long end = System.nanoTime();
                long requests = 0;
                long replies = 0;
                for(RPCThread thread : threads){
                    requests += thread.requests;
                    replies += thread.replies;
                }
                System.out.println("requests: "+requests);
                System.out.println("replies: "+replies);
                double throughput = (double)replies/ TimeUnit.SECONDS.convert(end-begin, TimeUnit.NANOSECONDS);
                System.out.println("throughput: "+throughput);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        private class RPCThread extends Thread{
            long requests, replies;
            @Override
            public void run(){
                latch.countDown();
                try{
                    latch.await();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                while(!Thread.interrupted()){
                    ++requests;
                    try{
                        client.call(null, HELLO_WORLD, null, null);
                        ++replies;
                    }catch(Throwable throwable){
                        ++replies;
                        if(throwable instanceof InterruptedException)
                            break;
                        throwable.printStackTrace();
                    }
                }
            }
        }
    }

    private static class NonBlocking implements Runnable, CallListener{
        private WAMPClient client;
        private RPCThread threads[];
        private CountDownLatch latch;
        private long duration;
        public NonBlocking(WAMPClient client, int threadCount, long duration){
            this.client = client;
            threads = new RPCThread[threadCount];
            latch = new CountDownLatch(threadCount+1);
            this.duration = duration;
        }

        @Override
        public void run(){
            for(int i=0; i<threads.length; i++)
                (threads[i]=new RPCThread()).start();
            try{
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
                long end = System.nanoTime();
                long requests = 0;
                for(RPCThread thread : threads){
                    requests += thread.requests.get();
                }

                int count = -1;
                while(true){
                    long replies = this.replies.get();
                    System.out.println(++count+" -------------------------------");
                    System.out.println("  requests: "+requests);
                    System.out.println("   replies: "+replies);
                    System.out.println("   waiting: "+client.waiting);
                    System.out.println("      sent: "+client.send);
                    double throughput = (double)replies/ TimeUnit.SECONDS.convert(end-begin, TimeUnit.NANOSECONDS);
                    System.out.println("throughput: " + throughput+"/sec");
                    if(requests==replies)
                        break;
                    Thread.sleep(10*1000);
                }
                System.out.println("completed");
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        private class RPCThread extends Thread{
            public AtomicLong requests = new AtomicLong();
            @Override
            public void run(){
                long count = 0;
                latch.countDown();
                try{
                    latch.await();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                while(!Thread.interrupted()){
                    ++count;
                    client.call(null, HELLO_WORLD, null, null, NonBlocking.this);
                }
                requests.set(count);
            }
        }

        private AtomicLong replies = new AtomicLong();

        @Override
        public void onResult(WAMPClient client, ResultMessage result){
            replies.incrementAndGet();
        }

        @Override
        public void onError(WAMPClient client, WAMPException error){
            replies.incrementAndGet();
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
            WAMPRouter router = new WAMPRouter(new NettyWebSocketServer(), uri);
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
            WAMPClient client = new WAMPClient(new NettyWebSocketClient(), uri, realm);
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

    public static void main(String[] args) throws Exception{
        if(args.length==0){
            System.err.println("needs arguments: {blocking} {threads} {timeout}");
            return;
        }
        final boolean blocking = Boolean.parseBoolean(args[0]);
        final int threads = Integer.parseInt(args[1]);
        final long timeout = Long.parseLong(args[2]);
        new WAMPClient(new NettyWebSocketClient(), uri, realm).connect(new SessionAdapter(){
            @Override
            public void onOpen(WAMPClient client){
                Runnable runnable;
                if(blocking)
                    runnable = new Blocking(client, threads, timeout);
                else
                    runnable = new NonBlocking(client, threads, timeout);
                new Thread(runnable).start();
            }
        });
    }
}

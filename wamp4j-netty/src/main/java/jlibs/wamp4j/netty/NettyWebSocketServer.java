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

package jlibs.wamp4j.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.AttributeKey;
import jlibs.wamp4j.spi.AcceptListener;
import jlibs.wamp4j.spi.WebSocketServer;

import java.net.URI;
import java.util.concurrent.ThreadFactory;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyWebSocketServer implements WebSocketServer{
    private static final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1, new ThreadFactory(){
        @Override
        public Thread newThread(Runnable r){
            return new Thread(r, "NettyWebSocketServer");
        }
    });

    private static final AttributeKey<AcceptListener> ACCEPT_LISTENER = AttributeKey.newInstance(AcceptListener.class.getName());
    private Channel channel;

    @Override
    public void bind(final URI uri, final String subProtocols[], final AcceptListener listener){
        int port = uri.getPort();
        if(port==-1)
            port = 80;
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception{
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(65536),
                                new NettyServerWebSocket(uri, listener, subProtocols)
                        );
                    }
                });
        bootstrap.bind(uri.getHost(), port).addListener(new ChannelFutureListener(){
            @Override
            public void operationComplete(ChannelFuture future) throws Exception{
                if(future.isSuccess()){
                    channel = future.channel();
                    channel.attr(ACCEPT_LISTENER).set(listener);
                    listener.onBind(NettyWebSocketServer.this);
                }else
                    listener.onError(future.cause());
            }
        });
    }

    @Override
    public void close(){
        channel.close().addListener(new ChannelFutureListener(){
            @Override
            public void operationComplete(ChannelFuture future) throws Exception{
                AcceptListener acceptListener = channel.attr(ACCEPT_LISTENER).get();
                if(!future.isSuccess())
                    acceptListener.onError(future.cause());
                acceptListener.onClose(NettyWebSocketServer.this);
            }
        });
    }
}

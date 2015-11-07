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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import jlibs.wamp4j.Util;
import jlibs.wamp4j.spi.ConnectListener;
import jlibs.wamp4j.spi.NamedThreadFactory;
import jlibs.wamp4j.spi.WAMPClientEndpoint;

import java.net.URI;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyClientEndpoint extends NettyEndpoint implements WAMPClientEndpoint{
    public NettyClientEndpoint(){
        super(NamedThreadFactory.CLIENT_THREAD_FACTORY);
    }

    @Override
    public void connect(final URI uri, final ConnectListener listener, final String... subProtocols){
        final SslContext sslContext;
        if("wss".equals(uri.getScheme())){
            try{
                if(sslSettings==null){
                    sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                }else{
                    sslContext = SslContextBuilder.forClient()
                            .trustManager(sslSettings.trustCertChainFile)
                            .keyManager(sslSettings.certificateFile, sslSettings.keyFile, sslSettings.keyPassword)
                            .build();
                }
            }catch(Throwable thr){
                listener.onError(thr);
                return;
            }
        }else if("ws".equals(uri.getScheme()))
            sslContext = null;
        else
            throw new IllegalArgumentException("invalid protocol: "+uri.getScheme());

        final int port = uri.getPort()==-1 ? (sslContext==null ? 80 : 443) : uri.getPort();

        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.MAX_MESSAGES_PER_READ, 50000)
                .option(ChannelOption.WRITE_SPIN_COUNT, 50000)
                .handler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception{
                        if(sslContext!=null)
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), uri.getHost(), port));
                        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri,
                                WebSocketVersion.V13, Util.toString(subProtocols),
                                false, new DefaultHttpHeaders());
                        ch.pipeline().addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                new WebSocketClientProtocolHandler(handshaker){
                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
                                        super.exceptionCaught(ctx, cause);
                                        listener.onError(cause);
                                    }
                                },
                                new HandshakeListener(handshaker, listener)
                        );
                    }
                });
        bootstrap.connect(uri.getHost(), port).addListener(new ChannelFutureListener(){
            @Override
            public void operationComplete(ChannelFuture future) throws Exception{
                if(!future.isSuccess()){
                    assert !future.channel().isOpen();
                    listener.onError(future.cause());
                }
            }
        });
    }

    private static class HandshakeListener extends SimpleChannelInboundHandler<Object>{
        private final WebSocketClientHandshaker handshaker;
        private final ConnectListener connectListener;

        public HandshakeListener(WebSocketClientHandshaker handshaker, ConnectListener connectListener){
            this.handshaker = handshaker;
            this.connectListener = connectListener;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg){}

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
            if(handshaker.isHandshakeComplete())
                super.exceptionCaught(ctx, cause);
            else{
                connectListener.onError(cause);
                ctx.close();
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
            if(evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE){
                NettyWebSocket webSocket = new NettyWebSocket(null, handshaker.actualSubprotocol());
                ctx.pipeline().addLast("ws-aggregator", new WebSocketFrameAggregator(16 * 1024 * 1024)); // 16MB
                ctx.pipeline().addLast("websocket", webSocket);
                ctx.pipeline().remove(this);
                webSocket.channelActive(ctx);
                connectListener.onConnect(webSocket);
            }else
                ctx.fireUserEventTriggered(evt);
        }
    }
}

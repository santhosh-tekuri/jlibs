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
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AttributeKey;
import jlibs.wamp4j.SSLSettings;
import jlibs.wamp4j.Util;
import jlibs.wamp4j.spi.AcceptListener;
import jlibs.wamp4j.spi.NamedThreadFactory;
import jlibs.wamp4j.spi.WAMPServerEndPoint;

import java.net.URI;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyServerEndpoint extends NettyEndpoint implements WAMPServerEndPoint{
    private static final AttributeKey<AcceptListener> ACCEPT_LISTENER = AttributeKey.newInstance(AcceptListener.class.getName());
    private Channel channel;

    public NettyServerEndpoint(){
        super(NamedThreadFactory.ROUTER_THREAD_FACTORY);
    }

    @Override
    public void bind(final URI uri, final String subProtocols[], final AcceptListener listener){
        final SslContext sslContext;
        if("wss".equals(uri.getScheme())){
            try{
                if(sslSettings==null){
                    SelfSignedCertificate ssc = new SelfSignedCertificate();
                    sslSettings = new SSLSettings().keyFile(ssc.privateKey()).certificateFile(ssc.certificate());
                }
                ClientAuth clientAuth = ClientAuth.values()[sslSettings.clientAuthentication.ordinal()];
                sslContext = SslContextBuilder.forServer(sslSettings.certificateFile, sslSettings.keyFile, sslSettings.keyPassword)
                                              .clientAuth(clientAuth)
                                              .trustManager(sslSettings.trustCertChainFile)
                                              .build();
            }catch(Throwable thr){
                listener.onError(thr);
                return;
            }
        }else if("ws".equals(uri.getScheme()))
            sslContext = null;
        else
            throw new IllegalArgumentException("invalid protocol: "+uri.getScheme());

        int port = uri.getPort();
        if(port==-1)
            port = sslContext==null ? 80 : 443;
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.MAX_MESSAGES_PER_READ, 50000)
                .childOption(ChannelOption.WRITE_SPIN_COUNT, 50000)
                .childHandler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception{
                        if(sslContext!=null)
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                        ch.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(65536),
                                new Handshaker(uri, listener, subProtocols)
                        );
                    }
                });
        bootstrap.bind(uri.getHost(), port).addListener(new ChannelFutureListener(){
            @Override
            public void operationComplete(ChannelFuture future) throws Exception{
                if(future.isSuccess()){
                    channel = future.channel();
                    channel.attr(ACCEPT_LISTENER).set(listener);
                    listener.onBind(NettyServerEndpoint.this);
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
                acceptListener.onClose(NettyServerEndpoint.this);
            }
        });
    }

    private static class Handshaker extends SimpleChannelInboundHandler<FullHttpRequest>{
        private final String scheme;
        private final String path;
        private final AcceptListener acceptListener;
        private final String subProtocols[];

        public Handshaker(URI uri, AcceptListener acceptListener, String subProtocols[]){
            scheme = uri.getScheme();
            path = uri.getPath().isEmpty() ? "/" : uri.getPath();
            this.acceptListener = acceptListener;
            this.subProtocols = subProtocols;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception{
            String host = request.headers().get("Host");
            String connection = request.headers().get("Connection");
            if(request.getMethod()!=HttpMethod.GET || !request.getUri().equals(path) || host==null || !"Upgrade".equals(connection)){
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.NOT_FOUND);
                HttpHeaders.setContentLength(response, 0);
                ChannelFuture future = ctx.writeAndFlush(response);
                if(!HttpHeaders.isKeepAlive(request))
                    future.addListener(ChannelFutureListener.CLOSE);
                return;
            }
            String url = scheme+"://"+ host +path;
            WebSocketServerHandshaker handshaker = new WebSocketServerHandshakerFactory(url,Util.toString(subProtocols), false).newHandshaker(request);
            if(handshaker==null){
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                return;
            }

            ChannelFuture future = handshaker.handshake(ctx.channel(), request);
            for(String subProtocol : subProtocols){
                if(subProtocol.equals(handshaker.selectedSubprotocol())){
                    NettyWebSocket webSocket = new NettyWebSocket(handshaker, subProtocol);
                    ctx.pipeline().addLast("ws-aggregator", new WebSocketFrameAggregator(16 * 1024 * 1024));
                    ctx.pipeline().addLast("websocket", webSocket);
                    ctx.pipeline().remove(this);
                    webSocket.channelActive(ctx);
                    acceptListener.onAccept(webSocket);
                    return;
                }
            }
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}

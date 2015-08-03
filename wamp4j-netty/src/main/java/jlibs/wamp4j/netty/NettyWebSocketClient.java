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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import jlibs.wamp4j.Util;
import jlibs.wamp4j.spi.ConnectListener;
import jlibs.wamp4j.spi.WebSocketClient;

import java.net.URI;
import java.util.concurrent.ThreadFactory;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyWebSocketClient implements WebSocketClient{
    private static final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1, new ThreadFactory(){
        @Override
        public Thread newThread(Runnable r){
            return new Thread(r, "NettyWebSocketClient");
        }
    });

    @Override
    public void connect(final URI uri, final ConnectListener listener, final String... subProtocols){
        String protocol = uri.getScheme();
        if(!protocol.equals("ws"))
            throw new IllegalArgumentException("invalid protocol: "+protocol);
        int port = uri.getPort();
        if(port==-1)
            port = 80;

        new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>(){
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception{
                        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri,
                                WebSocketVersion.V13, Util.toString(subProtocols),
                                false, new DefaultHttpHeaders());
                        ch.pipeline().addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                new WebSocketClientProtocolHandler(handshaker),
                                new WebSocketFrameAggregator(16 * 1024 * 1024), // 16MB
                                new NettyClientWebSocket(handshaker, listener)
                        );
                    }
                })
                .connect(uri.getHost(), port);
    }

    @Override
    public boolean isEventLoop(){
        return eventLoopGroup.next().inEventLoop();
    }

    @Override
    public void submit(Runnable r){
        eventLoopGroup.submit(r);
    }
}

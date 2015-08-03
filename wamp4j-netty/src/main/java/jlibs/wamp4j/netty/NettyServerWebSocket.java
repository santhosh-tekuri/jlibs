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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import jlibs.wamp4j.Util;
import jlibs.wamp4j.spi.AcceptListener;

import java.net.URI;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyServerWebSocket extends NettyWebSocket<Object>{
    private final URI uri;
    private final AcceptListener acceptListener;
    private final String subProtocols[];
    private WebSocketServerHandshaker handshaker;

    public NettyServerWebSocket(URI uri, AcceptListener acceptListener, String subProtocols[]){
        this.uri = uri;
        this.acceptListener = acceptListener;
        this.subProtocols = subProtocols;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception{
        if(msg instanceof FullHttpRequest){
            FullHttpRequest request = (FullHttpRequest)msg;

            handshaker = new WebSocketServerHandshakerFactory(getWebSocketLocation(request),
                    Util.toString(subProtocols), false).newHandshaker(request);
            if(handshaker==null){
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                return;
            }

            ChannelFuture future = handshaker.handshake(ctx.channel(), request);
            for(String subProtocol : subProtocols){
                if(subProtocol.equals(handshaker.selectedSubprotocol())){
                    this.subProtocol = subProtocol;
                    ctx.pipeline().addBefore(ctx.name(), "ws-aggregator", new WebSocketFrameAggregator(16 * 1024 * 1024));
                    acceptListener.onAccept(this);
                    return;
                }
            }
            future.addListener(ChannelFutureListener.CLOSE);
        }else if(msg instanceof WebSocketFrame){
            WebSocketFrame frame = (WebSocketFrame)msg;
            if(frame instanceof CloseWebSocketFrame)
                handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
            else if(frame instanceof PingWebSocketFrame)
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            else
                onWebSocketFrame(frame);
        }
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location =  req.headers().get("Host") + uri.getPath();
        return "ws://"+location; //@todo ssl
    }
}

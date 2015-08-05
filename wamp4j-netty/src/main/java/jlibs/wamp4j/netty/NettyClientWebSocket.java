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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import jlibs.wamp4j.spi.ConnectListener;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyClientWebSocket extends NettyWebSocket<WebSocketFrame>{
    private WebSocketClientHandshaker handshaker;
    private ConnectListener connectListener;

    public NettyClientWebSocket(WebSocketClientHandshaker handshaker, ConnectListener connectListener){
        this.handshaker = handshaker;
        this.connectListener = connectListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception{
        onWebSocketFrame(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        if(handshaker.isHandshakeComplete()){
            super.exceptionCaught(ctx, cause);
        }else{
            connectListener.onError(cause);
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
        if(evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE){
            subProtocol = handshaker.actualSubprotocol();
            connectListener.onConnect(this);
            connectListener = null;
        }else
            ctx.fireUserEventTriggered(evt);
    }
}

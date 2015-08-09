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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import jlibs.wamp4j.spi.Listener;
import jlibs.wamp4j.spi.MessageType;
import jlibs.wamp4j.spi.WebSocket;

import java.io.OutputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class NettyWebSocket<T> extends SimpleChannelInboundHandler<T> implements WebSocket{
    protected ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        this.ctx = ctx;
        if(listener!=null)
            listener.readyToWrite(this);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        super.channelInactive(ctx);
    }

    protected void onWebSocketFrame(WebSocketFrame msg) throws Exception{
        if(msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame){
            if(listener!=null){
                MessageType type = msg instanceof TextWebSocketFrame ? MessageType.text : MessageType.binary;
                ByteBufInputStream is = new ByteBufInputStream(msg.content());
                listener.onMessage(this, type, is);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
        if(listener!=null)
            listener.onReadComplete(this);
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        if(listener!=null)
            listener.onError(this, cause);
    }

    protected String subProtocol;

    @Override
    public String subProtocol(){
        return subProtocol;
    }

    protected Listener listener;

    @Override
    public void setListener(Listener listener){
        this.listener = listener;
    }

    @Override
    public OutputStream createOutputStream(){
        return new ByteBufOutputStream(ctx.alloc().buffer());
    }

    @Override
    public void release(OutputStream out){
        ByteBuf buffer = ((ByteBufOutputStream)out).buffer();
        buffer.release();
    }

    @Override
    public void send(MessageType type, OutputStream out){
        ByteBuf buffer = ((ByteBufOutputStream)out).buffer();
        WebSocketFrame frame = type==MessageType.text ? new TextWebSocketFrame(buffer) : new BinaryWebSocketFrame(buffer);
        ctx.write(frame).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public boolean isWritable(){
        return ctx.channel().isWritable();
    }

    @Override
    public void flush(){
        ctx.flush();
    }

    @Override
    public boolean isOpen(){
        return ctx.channel().isOpen();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception{
        if(ctx.channel().isWritable() && listener!=null)
            listener.readyToWrite(this);
    }

    @Override
    public void close(){
        ctx.writeAndFlush(new CloseWebSocketFrame()).addListener(ChannelFutureListener.CLOSE);
    }
}

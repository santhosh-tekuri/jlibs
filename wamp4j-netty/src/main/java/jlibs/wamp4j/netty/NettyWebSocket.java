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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.nio.AbstractNioChannel;
import io.netty.handler.codec.http.websocketx.*;
import jlibs.wamp4j.spi.Listener;
import jlibs.wamp4j.spi.MessageType;
import jlibs.wamp4j.spi.WAMPOutputStream;
import jlibs.wamp4j.spi.WAMPSocket;

import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NettyWebSocket extends ChannelInboundHandlerAdapter implements WAMPSocket{
    private final WebSocketServerHandshaker handshaker;
    private final String subProtocol;
    protected ChannelHandlerContext ctx;
    private ChannelPromise voidPromise;
    public NettyWebSocket(WebSocketServerHandshaker handshaker, String subProtocol){
        this.handshaker = handshaker;
        this.subProtocol = subProtocol;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        this.ctx = ctx;
        voidPromise = ctx.channel().voidPromise();
        if(listener!=null)
            listener.readyToWrite(this);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        if(listener!=null)
            listener.onClose(this);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        if(msg instanceof WebSocketFrame){
            WebSocketFrame frame = (WebSocketFrame)msg;
            try{
                if(frame instanceof TextWebSocketFrame || frame instanceof BinaryWebSocketFrame){
                    if(listener!=null){
                        ByteBufInputStream is = new ByteBufInputStream(frame.content());
                        MessageType type = frame instanceof TextWebSocketFrame ? MessageType.text : MessageType.binary;
                        listener.onMessage(this, type, is);
                    }
                }else if(frame instanceof PingWebSocketFrame)
                    ctx.write(new PongWebSocketFrame(frame.content().retain()));
                else if(frame instanceof CloseWebSocketFrame)
                    handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
            }finally{
                frame.release();
            }
        }else
            ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
        if(listener!=null)
            listener.onReadComplete(this);
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        if(listener!=null)
            listener.onError(this, cause);
    }

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
    public WAMPOutputStream createOutputStream(){
        return new NettyOutputStream(ctx.alloc().buffer());
    }

    @Override
    public void send(MessageType type, WAMPOutputStream out){
        ByteBuf buffer = ((NettyOutputStream)out).buffer;
        WebSocketFrame frame = type==MessageType.text ? new TextWebSocketFrame(buffer) : new BinaryWebSocketFrame(buffer);
        ctx.write(frame, voidPromise);
    }

    @Override
    public boolean isAutoRead(){
        return ctx.channel().config().isAutoRead();
    }

    @Override
    public void setAutoRead(boolean autoRead){
        ctx.channel().config().setAutoRead(autoRead);
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

    @Override
    public void kill(){
        try{
            Method method = AbstractNioChannel.class.getDeclaredMethod("javaChannel");
            method.setAccessible(true);
            SocketChannel channel = (SocketChannel)method.invoke(ctx.channel());
            channel.close();
        }catch(Exception ex){
            ex.printStackTrace();
            ctx.close();
        }
    }
}

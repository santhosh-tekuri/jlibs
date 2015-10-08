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

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import jlibs.wamp4j.spi.NamedThreadFactory;
import jlibs.wamp4j.spi.WAMPEndpoint;
import jlibs.wamp4j.spi.WAMPOutputStream;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class NettyEndpoint implements WAMPEndpoint{
    protected final NioEventLoopGroup eventLoopGroup;

    public NettyEndpoint(NamedThreadFactory threadFactory){
        eventLoopGroup = new NioEventLoopGroup(1, threadFactory);
    }

    @Override
    public boolean isEventLoop(){
        return eventLoopGroup.next().inEventLoop();
    }

    @Override
    public void submit(Runnable r){
        eventLoopGroup.submit(r);
    }

    @Override
    public WAMPOutputStream createOutputStream(){
        return new NettyOutputStream(PooledByteBufAllocator.DEFAULT.buffer());
    }
}

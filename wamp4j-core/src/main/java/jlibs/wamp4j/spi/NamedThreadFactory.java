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

package jlibs.wamp4j.spi;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Santhosh Kumar Tekuri
 */
public class NamedThreadFactory implements ThreadFactory{
    public static NamedThreadFactory CLIENT_THREAD_FACTORY = new NamedThreadFactory("WAMPClient");
    public static NamedThreadFactory ROUTER_THREAD_FACTORY = new NamedThreadFactory("WAMPRouter");

    private final AtomicLong counter = new AtomicLong();
    private final String name;

    public NamedThreadFactory(String name){
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r){
        return new Thread(r, name+counter.incrementAndGet());
    }
}

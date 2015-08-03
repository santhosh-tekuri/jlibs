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

package jlibs.wamp4j.router;

import jlibs.wamp4j.WAMPSerialization;
import jlibs.wamp4j.spi.AcceptListener;
import jlibs.wamp4j.spi.WebSocket;
import jlibs.wamp4j.spi.WebSocketServer;

import java.net.URI;

import static jlibs.wamp4j.Util.serialization;
import static jlibs.wamp4j.Util.subProtocols;

/**
 * todo: ssl
 * @author Santhosh Kumar Tekuri
 */
public class WAMPRouter{
    private WebSocketServer server;
    private URI uri;
    private WAMPSerialization serializations[];
    private Realms realms = new Realms();

    public WAMPRouter(WebSocketServer server, URI uri, WAMPSerialization... serializations){
        this.server = server;
        this.uri = uri;
        this.serializations = serializations;
    }

    public WAMPRouter(WebSocketServer server, URI uri){
        this(server, uri, WAMPSerialization.values());
    }

    public void bind(final RouterListener listener){
        server.bind(uri, subProtocols(serializations), new AcceptListener(){
            @Override
            public void onBind(WebSocketServer server){
                listener.onBind(WAMPRouter.this);
            }

            @Override
            public void onAccept(WebSocket webSocket){
                WAMPSerialization serialization = serialization(webSocket, serializations);
                webSocket.setListener(new Session(realms, webSocket, serialization));
            }

            @Override
            public void onError(Throwable error){
                listener.onError(WAMPRouter.this, error);
            }

            @Override
            public void onClose(WebSocketServer server){
                listener.onClose(WAMPRouter.this);
            }
        });
    }

    public void close(){
        // todo: reply on-flight requests
        server.close();
    }
}

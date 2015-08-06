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

package jlibs.wamp4j.client;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.*;
import jlibs.wamp4j.Util;
import jlibs.wamp4j.msg.*;
import jlibs.wamp4j.spi.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jlibs.wamp4j.Debugger.CLIENT;
import static jlibs.wamp4j.Util.serialization;
import static jlibs.wamp4j.Util.subProtocols;

/**
 * todo: ssl
 * @author Santhosh Kumar Tekuri
 */
public class WAMPClient{
    private WebSocketClient client;
    private URI uri;
    private String realm;
    private WAMPSerialization serializations[];

    public WAMPClient(WebSocketClient client, URI uri, String realm, WAMPSerialization... serializations){
        this.client = client;
        this.uri = uri;
        this.realm = realm;
        this.serializations = serializations;
    }

    public WAMPClient(WebSocketClient client, URI uri, String realm){
        this(client, uri, realm, WAMPSerialization.values());
    }

    private SessionListener sessionListener;
    private WebSocket webSocket;
    private WAMPSerialization serialization;
    private long sessionID = -1;
    private Map<Long, WAMPListener> requests = new HashMap<Long, WAMPListener>();
    private long lastUsedRequestID = -1;
    private Procedures procedures = new Procedures(this);
    private Topics topics = new Topics(this);

    public void connect(SessionListener listener){
        sessionListener = listener;
        client.connect(uri, connectListener, subProtocols(serializations));
    }

    private void disconnect(){
        if(CLIENT)
            Debugger.println(this, "-- disconnect");
        webSocket.close();
        webSocket = null;
        goodbyeSend = false;
        if(sessionID!=-1){
            sessionID = -1;
            sessionListener.onClose(WAMPClient.this);
        }
    }

    private final ConnectListener connectListener = new ConnectListener(){
        @Override
        public void onConnect(WebSocket webSocket){
            WAMPClient.this.webSocket = webSocket;
            WAMPClient.this.serialization = serialization(webSocket, serializations);
            webSocket.setListener(messageListener);
            try{
                send(new HelloMessage(realm, Peer.client.details));
            }catch(WAMPException impossible){
                impossible.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable error){
            error.printStackTrace();
        }
    };

    private final Listener messageListener = new Listener(){
        @Override
        public void onMessage(WebSocket webSocket, MessageType type, InputStream is){
            if(type!=serialization.messageType()){
                onError(webSocket, new RuntimeException("unexpected messageType: " + type));
                return;
            }

            WAMPMessage message;
            try{
                ArrayNode array = serialization.mapper().readValue(is, ArrayNode.class);
                message = WAMPMessageDecoder.decode(array);
            }catch(Throwable thr){
                onError(webSocket, thr);
                return;
            }

            if(CLIENT)
                Debugger.println(WAMPClient.this, "<- %s", message);
            switch(message.getID()){
                case WelcomeMessage.ID:
                    WelcomeMessage welcome = (WelcomeMessage)message;
                    assert sessionID==-1;
                    sessionID = welcome.sessionID;
                    sessionListener.onOpen(WAMPClient.this);
                    if(userClosed)
                        doClose();
                    break;
                case AbortMessage.ID:
                    AbortMessage abort = (AbortMessage)message;
                    assert sessionID==-1;
                    sessionListener.onError(WAMPClient.this, new WAMPException(new ErrorCode(abort)));
                    disconnect();
                    break;
                case ErrorMessage.ID:
                    ErrorMessage error = (ErrorMessage)message;
                    WAMPListener listener = requests.remove(error.requestID);
                    listener.onError(WAMPClient.this, new WAMPException(error));
                    break;
                case GoodbyeMessage.ID:
                    if(!goodbyeSend){
                        try{
                            send(new GoodbyeMessage("good-bye", ErrorCode.GOODBYE_AND_OUT));
                        }catch(WAMPException ex){
                            sessionListener.onWarning(WAMPClient.this, ex);
                        }
                        cleanup();
                    }
                    disconnect();
                    break;
                case RegisteredMessage.ID:
                    RegisteredMessage registered = (RegisteredMessage)message;
                    Procedure procedure = (Procedure)requests.remove(registered.requestID);
                    procedures.onRegister(procedure, registered.registrationID);
                    break;
                case UnregisteredMessage.ID:
                    UnregisteredMessage unregistered = (UnregisteredMessage)message;
                    procedure = (Procedure)requests.remove(unregistered.requestID);
                    procedures.onUnregister(procedure);
                    break;
                case InvocationMessage.ID:
                    InvocationMessage invocation = (InvocationMessage)message;
                    procedures.onInvocation(invocation);
                    break;
                case ResultMessage.ID:
                    ResultMessage result = (ResultMessage)message;
                    CallListener callListener = (CallListener)requests.remove(result.requestID);
                    callListener.onResult(WAMPClient.this, result);
                    break;
                case SubscribedMessage.ID:
                    SubscribedMessage subscribed = (SubscribedMessage)message;
                    Subscription subscription = (Subscription)requests.remove(subscribed.requestID);
                    topics.onSubscribe(subscribed.subscriptionID, subscription);
                    break;
                case UnsubscribedMessage.ID:
                    UnsubscribedMessage unsubscribed = (UnsubscribedMessage)message;
                    subscription = (Subscription)requests.remove(unsubscribed.requestID);
                    topics.onUnsubscribe(subscription);
                    break;
                case PublishedMessage.ID:
                    PublishedMessage published = (PublishedMessage)message;
                    PublishListener publishListener = (PublishListener)requests.remove(published.requestID);
                    publishListener.onPublish(WAMPClient.this);
                    break;
                case EventMessage.ID:
                    EventMessage event = (EventMessage)message;
                    topics.onEvent(event);
                    break;
                default:
                    if(CLIENT)
                        Debugger.println(WAMPClient.this, "-- %s not yet implemented");
            }
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error){
            sessionListener.onError(WAMPClient.this, new WAMPException(error));
            disconnect();
        }
    };

    private void send(WAMPMessage message) throws WAMPException{
        OutputStream out = webSocket.createOutputStream();
        try{
            serialization.mapper().writeValue(out, message.toArrayNode());
        }catch(Throwable thr){
            webSocket.release(out);
            throw new WAMPException(ErrorCode.serializationFailed()).initCause(thr);
        }
        if(CLIENT)
            Debugger.println(this, "-> %s", message);
        webSocket.send(serialization.messageType(), out);
        webSocket.flush();
    }

    private boolean validate(WAMPListener listener){
        if(sessionID==-1){
            listener.onError(this, new WAMPException(ErrorCode.notConnected()));
            return false;
        }else
            return true;
    }

    private void validate(){
        if(sessionID==-1)
            throw new IllegalStateException("WAMPClient not connected");
    }

    public void register(final ObjectNode options, final Procedure procedure){
        if(!validate(procedure))
            return;
        if(client.isEventLoop()){
            lastUsedRequestID = Util.generateID(requests, lastUsedRequestID);
            RegisterMessage register = new RegisterMessage(lastUsedRequestID, options, procedure.uri);
            requests.put(lastUsedRequestID, procedure);
            try{
                send(register);
            }catch(WAMPException ex){
                requests.remove(lastUsedRequestID).onError(this, ex);
            }
        }else{
            client.submit(new Runnable(){
                @Override
                public void run(){
                    register(options, procedure);
                }
            });
        }
    }

    public void unregister(final Procedure procedure){
        if(!validate(procedure))
            return;
        if(client.isEventLoop()){
            lastUsedRequestID = Util.generateID(requests, lastUsedRequestID);
            UnregisterMessage unregister = new UnregisterMessage(lastUsedRequestID, procedure.registrationID);
            requests.put(lastUsedRequestID, procedure);
            try{
                send(unregister);
            }catch(WAMPException ex){
                procedure.onError(this, ex);
            }
        }else{
            client.submit(new Runnable(){
                @Override
                public void run(){
                    unregister(procedure);
                }
            });
        }
    }

    public AtomicLong called = new AtomicLong();
    public AtomicLong processed = new AtomicLong();
    public void call(final ObjectNode options, final String procedure, final ArrayNode arguments, final ObjectNode argumentsKw, final CallListener listener){
        if(!validate(listener))
            return;
        if(client.isEventLoop()){
            processed.incrementAndGet();
            lastUsedRequestID = Util.generateID(requests, lastUsedRequestID);
            CallMessage call = new CallMessage(lastUsedRequestID, options, procedure, arguments, argumentsKw);
            requests.put(lastUsedRequestID, listener);
            try{
                send(call);
            }catch(WAMPException ex){
                requests.remove(lastUsedRequestID).onError(this, ex);
            }
        }else{
            called.incrementAndGet();
            client.submit(new Runnable(){
                @Override
                public void run(){
                    call(options, procedure, arguments, argumentsKw, listener);
                }
            });
        }
    }

    public ResultMessage call(ObjectNode options, String procedure, ArrayNode arguments, ObjectNode argumentsKw) throws WAMPException, InterruptedException{
        if(sessionID==-1)
            throw new WAMPException(ErrorCode.notConnected());
        if(client.isEventLoop())
            throw new WAMPException(ErrorCode.wrongThread());
        BlockingCallListener listener = new BlockingCallListener();
        synchronized(listener){
            call(options, procedure, arguments, argumentsKw, listener);
            listener.wait();
        }
        if(listener.error!=null)
            throw listener.error;
        return listener.result;
    }

    public void reply(final YieldMessage yield){
        validate();
        if(client.isEventLoop()){
            try{
                send(yield);
            }catch(WAMPException ex){
                sessionListener.onWarning(this, ex);
                try{
                    send(new ErrorMessage(InvocationMessage.ID, yield.requestID, ex.getErrorCode()));
                }catch(WAMPException error){
                    sessionListener.onError(this, error);
                    disconnect();
                }
            }
        }else{
            client.submit(new Runnable(){
                @Override
                public void run(){
                    reply(yield);
                }
            });
        }
    }

    public void reply(final ErrorMessage error){
        validate();
        if(client.isEventLoop()){
            try{
                send(error);
            }catch(WAMPException ex){
                sessionListener.onError(this, ex);
                webSocket.close();
            }
        }else{
            client.submit(new Runnable(){
                @Override
                public void run(){
                    reply(error);
                }
            });
        }
    }

    public void subscribe(final ObjectNode options, final Subscription subscription){
        if(!validate(subscription))
            return;
        if(client.isEventLoop()){
            Topic topic = topics.get(subscription.topic);
            if(topic==null){
                lastUsedRequestID = Util.generateID(requests, lastUsedRequestID);
                SubscribeMessage subscribe = new SubscribeMessage(lastUsedRequestID, options, subscription.topic);
                requests.put(lastUsedRequestID, subscription);
                try{
                    send(subscribe);
                }catch(WAMPException ex){
                    requests.remove(lastUsedRequestID).onError(this, ex);
                }
            }else{
                topic.onSubscribe(subscription);
            }
        }else{
            client.submit(new Runnable(){
                @Override
                public void run(){
                    subscribe(options, subscription);
                }
            });
        }
    }

    public void unsubscribe(final Subscription subscription){
        if(!validate(subscription))
            return;
        if(client.isEventLoop()){
            Topic topic = topics.get(subscription.subscriptionID);
            if(topic==null || topic.size()<=1){
                lastUsedRequestID = Util.generateID(requests, lastUsedRequestID);
                UnsubscribeMessage unsubscribe = new UnsubscribeMessage(lastUsedRequestID, subscription.subscriptionID);
                requests.put(lastUsedRequestID, subscription);
                try{
                    send(unsubscribe);
                }catch(WAMPException ex){
                    requests.remove(lastUsedRequestID).onError(this, ex);
                }
            }else{
                topic.onUnsubscribe(subscription);
            }
        }else{
            client.submit(new Runnable(){
                @Override
                public void run(){
                    unsubscribe(subscription);
                }
            });
        }
    }

    public void publish(final ObjectNode options, final String topic, final ArrayNode arguments, final ObjectNode argumentsKw, final PublishListener listener){
        if(!validate(listener))
            return;
        if(client.isEventLoop()){
            Topic t = topics.get(topic);
            if(t!=null){
                EventMessage event = new EventMessage(t.subscriptionID, -1, options, arguments, argumentsKw);
                t.onEvent(event);
            }
            lastUsedRequestID = Util.generateID(requests, lastUsedRequestID);
            PublishMessage publish = new PublishMessage(lastUsedRequestID, options, topic, arguments, argumentsKw);
            try{
                send(publish);
                if(publish.needsAcknowledgement())
                    requests.put(lastUsedRequestID, listener);
                else
                    listener.onPublish(this);
            }catch(WAMPException ex){
                listener.onError(this, ex);
            }
        }else{
            client.submit(new Runnable(){
                @Override
                public void run(){
                    publish(options, topic, arguments, argumentsKw, listener);
                }
            });
        }
    }

    private boolean userClosed;
    private boolean goodbyeSend = false;
    public void close(){
        if(client.isEventLoop()){
            userClosed = true;
            if(sessionID!=-1)
                doClose();
        }else{
            client.submit(new Runnable(){
                @Override
                public void run(){
                    close();
                }
            });
        }
    }

    private void cleanup(){
        if(CLIENT)
            Debugger.println(this, "-- closing listeners");
        WAMPException error = new WAMPException(ErrorCode.systemShutdown());
        for(Map.Entry<Long, WAMPListener> entry : requests.entrySet()){
            WAMPListener listener = entry.getValue();
            listener.onError(this, error);
        }
        requests.clear();
        lastUsedRequestID = -1;
        procedures.unregisterAll();
        topics.unsubscribeAll();
    }

    private void doClose(){
        if(CLIENT)
            Debugger.println(this, "-- doClose");

        cleanup();
        WAMPMessage message = new GoodbyeMessage("good-bye", ErrorCode.GOODBYE_AND_OUT);
        goodbyeSend = true;
        try{
            send(message);
        }catch(WAMPException ex){
            sessionListener.onWarning(this, ex);
            disconnect();
        }
    }

    @Override
    public String toString(){
        return String.format("%s[%s|%d]", getClass().getSimpleName(), realm, sessionID);
    }
}

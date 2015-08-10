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

import com.fasterxml.jackson.databind.node.ArrayNode;
import jlibs.wamp4j.*;
import jlibs.wamp4j.msg.*;
import jlibs.wamp4j.spi.Listener;
import jlibs.wamp4j.spi.MessageType;
import jlibs.wamp4j.spi.WebSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static jlibs.wamp4j.Debugger.ROUTER;

/**
 * @author Santhosh Kumar Tekuri
 */
class Session implements Listener{
    private final WAMPRouter router;
    private final WebSocket webSocket;
    private final WAMPSerialization serialization;

    private Realm realm;
    long sessionID;

    // key = registrationID
    protected final Map<Long, Procedure> procedures = new HashMap<Long, Procedure>();
    private long lastRegistrationID = -1;

    // key = invocationRequestID
    protected final Map<Long, CallRequest> requests = new HashMap<Long, CallRequest>();
    private long lastRequestID = -1;

    // key = subscriptionID
    protected final Map<Long, Topic> subscriptions = new HashMap<Long, Topic>();

    public Session(WAMPRouter router, WebSocket webSocket, WAMPSerialization serialization){
        this.router = router;
        this.webSocket = webSocket;
        this.serialization = serialization;
        array = router.array;
    }

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

        if(ROUTER)
            Debugger.println(this, "<- %s", message);
        switch(message.getID()){
            case HelloMessage.ID:
                HelloMessage hello = (HelloMessage)message;
                realm = router.realms.get(hello.realm);
                realm.addSession(this);
                WelcomeMessage welcome = new WelcomeMessage(sessionID, Peer.router.details);
                send(welcome);
                break;
            case AbortMessage.ID:
                webSocket.close();
                break;
            case GoodbyeMessage.ID:
                close();
                realm.removeSession(this);
                break;
            case RegisterMessage.ID:
                RegisterMessage register = (RegisterMessage)message;
                WAMPMessage reply;
                if(realm.procedures.containsKey(register.procedure))
                    reply = register.error(ErrorCode.procedureAlreadyExists(register.procedure));
                else
                    reply = addProcedure(register);
                send(reply);
                break;
            case UnregisterMessage.ID:
                UnregisterMessage unregister = (UnregisterMessage)message;
                Procedure procedure = procedures.remove(unregister.registrationID);
                if(procedure==null)
                    reply = unregister.error(ErrorCode.noSuchRegistration(unregister.registrationID));
                else{
                    realm.procedures.remove(procedure.register.procedure);
                    reply = new UnregisteredMessage(unregister.requestID);
                    // notify waiting callers if any
                    for(Map.Entry<Long, CallRequest> entry : procedure.requests.entrySet()){
                        requests.remove(entry.getKey());
                        CallRequest callRequest = entry.getValue();
                        callRequest.callSession.send(callRequest.noSuchProcedure());
                    }
                }
                send(reply);
                break;
            case CallMessage.ID:
                CallMessage call = (CallMessage)message;
                MetaProcedure metaProcedure = MetaProcedures.get(call.procedure);
                if(metaProcedure==null){
                    procedure = realm.procedures.get(call.procedure);
                    if(procedure==null)
                        send(call.error(ErrorCode.noSuchProcedure(call.procedure)));
                    else{
                        long invocationRequestId = procedure.session.addRequest(call.requestID, this, procedure);
                        reply = new InvocationMessage(invocationRequestId, procedure.registrationID, call.options, call.arguments, call.argumentsKw);
                        procedure.session.send(reply);
                    }
                }else
                    metaProcedure.reply(this, call);
                break;
            case YieldMessage.ID:
                YieldMessage yield = (YieldMessage)message;
                CallRequest callRequest = requests.remove(yield.requestID);
                if(callRequest ==null)
                    return;
                callRequest.reply(yield);
                break;
            case ErrorMessage.ID:
                ErrorMessage error = (ErrorMessage)message;
                callRequest = requests.remove(error.requestID);
                if(callRequest ==null)
                    return;
                callRequest.reply(error);
                break;
            case SubscribeMessage.ID:
                SubscribeMessage subscribe = (SubscribeMessage)message;
                long subscriptionID = realm.topics.subscribe(this, subscribe.topic);
                SubscribedMessage subscribed = new SubscribedMessage(subscribe.requestID, subscriptionID);
                send(subscribed);
                break;
            case UnsubscribeMessage.ID:
                UnsubscribeMessage unsubscribe = (UnsubscribeMessage)message;
                if(realm.topics.unsubscribe(this, unsubscribe.subscriptionID))
                    reply = new UnsubscribedMessage(unsubscribe.requestID);
                else
                    reply = unsubscribe.error(ErrorCode.noSuchSubscription(unsubscribe.subscriptionID));
                send(reply);
                break;
            case PublishMessage.ID:
                PublishMessage publish = (PublishMessage)message;
                realm.topics.publish(this, publish);
                if(publish.needsAcknowledgement()){
                    PublishedMessage published = new PublishedMessage(publish.requestID, 0);
                    send(published);
                }
                break;
            default:
                if(ROUTER)
                    Debugger.println(this, "-- not yet implemented%n");
        }
    }

    @Override
    public void onReadComplete(WebSocket webSocket){
        Session session;
        while((session=router.removeFromFlushList())!=null)
            session.webSocket.flush();
    }

    @Override
    public void readyToWrite(WebSocket webSocket){}

    @Override
    public void onError(WebSocket webSocket, Throwable error){
        router.listener.onError(router, error);
        cleanup();
        realm.removeSession(this);
        webSocket.close();
    }

    private boolean goodbyeSend = false;
    protected boolean flushNeeded;
    protected Session flushNext;
    private final ArrayNode array;
    protected boolean send(WAMPMessage message){
        if(sessionID==-1)
            return false;
        if(message instanceof GoodbyeMessage)
            goodbyeSend = true;
        OutputStream out = webSocket.createOutputStream();
        try{
            array.removeAll();
            message.toArrayNode(array);
            serialization.mapper().writeValue(out, array);
        }catch(Throwable thr){
            if(flushNeeded)
                router.removeFromFlushList(this);
            router.listener.onError(router, thr);
            webSocket.release(out);
            cleanup();
            realm.removeSession(this);
            webSocket.close();
            return false;
        }
        if(!flushNeeded)
            router.addToFlushList(this);
        if(ROUTER)
            Debugger.println(this, "-> %s", message);
        webSocket.send(serialization.messageType(), out);
        return true;
    }

    protected RegisteredMessage addProcedure(RegisterMessage register){
        lastRegistrationID = Util.generateID(procedures, lastRegistrationID);
        Procedure procedure = new Procedure(register, lastRegistrationID, this);
        procedures.put(lastRegistrationID, procedure);
        realm.procedures.put(register.procedure, procedure);
        return new RegisteredMessage(register.requestID, lastRegistrationID);
    }

    protected long addRequest(long callID, Session callSession, Procedure procedure){
        lastRequestID = Util.generateID(requests, lastRequestID);
        CallRequest callRequest = new CallRequest(callID, procedure, callSession);
        requests.put(lastRequestID, callRequest);
        procedure.requests.put(lastRequestID, callRequest);
        return lastRequestID;
    }

    private void cleanup(){
        if(ROUTER)
            Debugger.println(this, "-- notify waiting callers");
        for(Map.Entry<Long, CallRequest> entry : requests.entrySet()){
            CallRequest callRequest = entry.getValue();
            callRequest.procedure.requests.remove(entry.getKey());
            callRequest.callSession.send(callRequest.noSuchProcedure());
        }
        for(Procedure procedure : procedures.values())
            realm.procedures.remove(procedure.uri());
        while(!subscriptions.isEmpty())
            realm.topics.unsubscribe(this, subscriptions.keySet().iterator().next());
    }

    public void close(){
        cleanup();
        if(!goodbyeSend){
            if(send(new GoodbyeMessage("good-bye", ErrorCode.GOODBYE_AND_OUT)))
                webSocket.close();
        }
    }

    public Realm realm(){
        return realm;
    }

    @Override
    public String toString(){
        return String.format("%s[%s|%d]", getClass().getSimpleName(), realm, sessionID);
    }
}
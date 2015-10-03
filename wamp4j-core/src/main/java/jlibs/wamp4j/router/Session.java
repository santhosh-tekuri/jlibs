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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jlibs.wamp4j.Debugger;
import jlibs.wamp4j.Peer;
import jlibs.wamp4j.Util;
import jlibs.wamp4j.WAMPSerialization;
import jlibs.wamp4j.error.ErrorCode;
import jlibs.wamp4j.error.InvalidMessageException;
import jlibs.wamp4j.msg.*;
import jlibs.wamp4j.spi.Listener;
import jlibs.wamp4j.spi.MessageType;
import jlibs.wamp4j.spi.WAMPOutputStream;
import jlibs.wamp4j.spi.WAMPSocket;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static jlibs.wamp4j.Debugger.ROUTER;

/**
 * @author Santhosh Kumar Tekuri
 */
class Session implements Listener{
    private final WAMPRouter router;
    private final WAMPSocket socket;
    protected final WAMPSerialization serialization;

    private Realm realm;
    long sessionID = -1;

    // key = registrationID
    protected final Map<Long, Procedure> procedures = new HashMap<Long, Procedure>();
    private long lastRegistrationID = -1;

    // key = invocationRequestID
    protected final Map<Long, CallRequest> requests = new HashMap<Long, CallRequest>();
    private long lastRequestID = -1;

    // key = subscriptionID
    protected final Map<Long, Topic> subscriptions = new HashMap<Long, Topic>();

    public Session(WAMPRouter router, WAMPSocket socket, WAMPSerialization serialization){
        this.router = router;
        this.socket = socket;
        this.serialization = serialization;
        array = router.array;
    }

    private int autoRead = 0;
    private Map<Long, Session> blockedReaders = new HashMap<Long, Session>();

    @Override
    public void onMessage(WAMPSocket socket, MessageType type, InputStream is){
        router.readingSession = this;
        if(type!=serialization.messageType()){
            onError(socket, new RuntimeException("unexpected messageType: " + type));
            return;
        }

        JsonParser parser = null;
        try{
            parser = serialization.mapper().getFactory().createParser(is);
            if(parser.nextToken()!=JsonToken.START_ARRAY)
                throw new InvalidMessageException();
            int id = parser.nextIntValue(-1);
            switch(id){
                case HelloMessage.ID:
                    String uri = parser.nextTextValue();
                    if(uri==null)
                        throw new InvalidMessageException();
                    realm = router.realms.get(uri);
                    if(ROUTER)
                        Debugger.println(this, "<- HelloMessage: [%d, \"%s\", ...]", id, uri);
                    realm.addSession(this);
                    send(welcomeMessage());
                    break;
                case AbortMessage.ID:
                    if(ROUTER)
                        Debugger.println(this, "<- AbortMessage: [%d, ...]", id);
                    socket.close();
                    break;
                case GoodbyeMessage.ID:
                    if(ROUTER)
                        Debugger.println(this, "<- GoodbyeMessage: [%d, ...]", id);
                    close();
                    realm.removeSession(this);
                    parser.close();
                    break;
                case RegisterMessage.ID:
                    long requestID = parser.nextLongValue(-1);
                    if(requestID==-1)
                        throw new InvalidMessageException();
                    if(parser.nextToken()!=JsonToken.START_OBJECT)
                        throw new InvalidMessageException();
                    ObjectNode options = serialization.mapper().readTree(parser);
                    uri = parser.nextTextValue();
                    if(ROUTER)
                        Debugger.println(this, "<- RegisterMessage: [%d, %d, %s, \"%s\", ...]", id, requestID, options, uri);
                    if(realm.procedures.containsKey(uri))
                        send(errorMessage(id, requestID, ErrorCode.procedureAlreadyExists(uri)));
                    else{
                        RegisterMessage register = new RegisterMessage(requestID, options, uri);
                        RegisteredMessage registered = addProcedure(register);
                        send(registeredMessage(requestID, registered.registrationID));
                    }
                    break;
                case UnregisterMessage.ID:
                    requestID = parser.nextLongValue(-1);
                    if(requestID==-1)
                        throw new InvalidMessageException();
                    long registrationID = parser.nextLongValue(-1);
                    if(registrationID==-1)
                        throw new InvalidMessageException();
                    Procedure procedure = procedures.remove(registrationID);
                    if(ROUTER)
                        Debugger.println(this, "<- UnregisterMessage: [%d, %d, ...]", id, requestID, registrationID);
                    if(procedure==null)
                        send(errorMessage(id, requestID, ErrorCode.noSuchRegistration(registrationID)));
                    else{
                        realm.procedures.remove(procedure.register.procedure);
                        // notify waiting callers if any
                        for(Map.Entry<Long, CallRequest> entry : procedure.requests.entrySet()){
                            requests.remove(entry.getKey());
                            CallRequest callRequest = entry.getValue();
                            callRequest.callSession.send(callRequest.noSuchProcedure());
                        }
                        send(unregisteredMessage(requestID));
                    }
                    break;
                case CallMessage.ID:
                    requestID = parser.nextLongValue(-1);
                    if(requestID==-1)
                        throw new InvalidMessageException();
                    if(parser.nextToken()!=JsonToken.START_OBJECT)
                        throw new InvalidMessageException();
                    options = serialization.mapper().readTree(parser);
                    uri = parser.nextTextValue();
                    if(ROUTER)
                        Debugger.println(this, "<- CallMessage: [%d, %d, %s, \"%s\", ...]", id, requestID, options, uri);
                    procedure = realm.procedures.get(uri);
                    if(procedure==null){
                        MetaProcedure metaProcedure = MetaProcedures.get(uri);
                        if(metaProcedure==null)
                            send(errorMessage(id, requestID, ErrorCode.noSuchProcedure(uri)));
                        else{
                            ArrayNode arguments = null;
                            if(parser.nextToken()!=JsonToken.END_ARRAY)
                                arguments = serialization.mapper().readTree(parser);
                            ObjectNode argumentsKw = null;
                            if(parser.nextToken()!=JsonToken.END_ARRAY)
                                argumentsKw = serialization.mapper().readTree(parser);
                            CallMessage call = new CallMessage(requestID, options, uri, arguments, argumentsKw);
                            metaProcedure.reply(this, call);
                        }
                    }else{
                        long invocationRequestId = procedure.session.addRequest(requestID, this, procedure);
                        procedure.session.send(invocationMessage(invocationRequestId, procedure.registrationID.longValue(), options, parser));

                    }
                    break;
                case YieldMessage.ID:
                    requestID = parser.nextLongValue(-1);
                    if(requestID==-1)
                        throw new InvalidMessageException();
                    if(ROUTER)
                        Debugger.println(this, "<- YieldMessage: [%d, %d, ...]", id, requestID);
                    CallRequest callRequest = requests.remove(requestID);
                    if(callRequest==null)
                        return;
                    callRequest.reply(requestID, parser);
                    break;
                case ErrorMessage.ID:
                    long requestType = parser.nextLongValue(-1);
                    if(requestType!=InvocationMessage.ID)
                        throw new InvalidMessageException();
                    requestID = parser.nextLongValue(-1);
                    if(requestID==-1)
                        throw new InvalidMessageException();
                    if(ROUTER)
                        Debugger.println(this, "<- ErrorMessage: [%d, %d, %d, ...]", id, requestType, requestID);
                    callRequest = requests.remove(requestID);
                    if(callRequest==null)
                        return;
                    callRequest.error(requestID, parser);
                    break;
                case SubscribeMessage.ID:
                    requestID = parser.nextLongValue(-1);
                    if(requestID==-1)
                        throw new InvalidMessageException();
                    if(parser.nextToken()!=JsonToken.START_OBJECT)
                        throw new InvalidMessageException();
                    options = serialization.mapper().readTree(parser);
                    uri = parser.nextTextValue();
                    if(ROUTER)
                        Debugger.println(this, "<- SubscribeMessage: [%d, %d, %s, \"%s\", ...]", id, requestID, options, uri);
                    long subscriptionID = realm.topics.subscribe(this, uri);
                    send(subscribedMessage(requestID, subscriptionID));
                    break;
                case UnsubscribeMessage.ID:
                    requestID = parser.nextLongValue(-1);
                    if(requestID==-1)
                        throw new InvalidMessageException();
                    subscriptionID = parser.nextLongValue(-1);
                    if(subscriptionID==-1)
                        throw new InvalidMessageException();
                    if(ROUTER)
                        Debugger.println(this, "<- UnsubscribeMessage: [%d, %d, ...]", id, requestID, subscriptionID);
                    if(realm.topics.unsubscribe(this, subscriptionID))
                        send(unsubscribedMessage(requestID));
                    else
                        send(errorMessage(id, requestID, ErrorCode.noSuchSubscription(subscriptionID)));
                    break;
                case PublishMessage.ID:
                    requestID = parser.nextLongValue(-1);
                    if(requestID==-1)
                        throw new InvalidMessageException();
                    if(parser.nextToken()!=JsonToken.START_OBJECT)
                        throw new InvalidMessageException();
                    options = serialization.mapper().readTree(parser);
                    if(ROUTER)
                        Debugger.println(this, "<- PublishMessage: [%d, %d, %s, ...]", id, requestID, options);
                    realm.topics.publish(this, options, parser);
                    if(PublishMessage.needsAcknowledgement(options))
                        send(publishedMessage(requestID, 0));
                    break;
                default:
                    if(ROUTER)
                        Debugger.println(this, "<- UnknownMessage: [%d, ...]", id);
                    if(id==-1)
                        throw new InvalidMessageException();
            }
        }catch(Throwable thr){
            onError(socket, thr);
        }finally{
            try{
                if(parser!=null)
                    parser.close();
            }catch(Throwable thr){
                router.listener.onWarning(router, thr);
            }
        }
    }

    @Override
    public void onReadComplete(WAMPSocket socket){
        assert autoRead==0;
        router.readingSession = null;
        Session session;
        while((session=router.removeFromFlushList())!=null){
            session.socket.flush();
            if(!session.socket.isWritable()){
                ++autoRead;
                session.blockedReaders.put(sessionID, this);
            }
        }
        if(socket.isAutoRead()!=(autoRead==0))
            socket.setAutoRead(autoRead==0);
    }

    @Override
    public void readyToWrite(WAMPSocket socket){
        if(!blockedReaders.isEmpty()){
            for(Session session : blockedReaders.values()){
                assert session.autoRead>0;
                if(--session.autoRead==0)
                    session.socket.setAutoRead(true);
            }
            blockedReaders.clear();
        }
    }

    @Override
    public void onError(WAMPSocket socket, Throwable error){
        if(ROUTER)
            Debugger.println(this, "-- onError: "+error.getMessage());
        router.listener.onWarning(router, error);
        cleanup();
        realm.removeSession(this);
        socket.close();
    }

    @Override
    public void onClose(WAMPSocket socket){
        if(ROUTER)
            Debugger.println(this, "-- onClose");
        assert !socket.isOpen();
        if(sessionID!=-1){
            cleanup();
            realm.removeSession(this);
        }
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
        WAMPOutputStream out = socket.createOutputStream();
        try{
            array.removeAll();
            message.toArrayNode(array);
            serialization.mapper().writeValue(out, array);
        }catch(Throwable thr){
            if(flushNeeded)
                router.removeFromFlushList(this);
            router.listener.onError(router, thr);
            out.release();
            cleanup();
            realm.removeSession(this);
            socket.close();
            return false;
        }
        if(!flushNeeded)
            router.addToFlushList(this);
        if(ROUTER)
            Debugger.println(this, "-> %s", message);
        socket.send(serialization.messageType(), out);
        if(!socket.isWritable()){
            socket.flush();
            if(router.readingSession!=null && socket.isWritable())
                router.readingSession.socket.setAutoRead(false);
        }
        return true;
    }

    protected void send(WAMPOutputStream out){
        if(!flushNeeded)
            router.addToFlushList(this);
        if(ROUTER)
            Debugger.println(this, "%s", Debugger.temp);
        socket.send(serialization.messageType(), out);
        if(!socket.isWritable()){
            socket.flush();
            if(router.readingSession!=null && socket.isWritable())
                router.readingSession.socket.setAutoRead(false);
        }
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
        readyToWrite(socket); // wakeup blocked readers if any
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
                socket.close();
        }
    }

    public Realm realm(){
        return realm;
    }

    @Override
    public String toString(){
        return String.format("%s[%s|%d]", getClass().getSimpleName(), realm, sessionID);
    }

    protected WAMPOutputStream numbers(int id, long number1) throws Throwable{
        WAMPOutputStream out = socket.createOutputStream();
        try{
            JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
            json.writeStartArray();
            json.writeNumber(id);
            json.writeNumber(number1);
            json.writeEndArray();
            json.close();
            return out;
        }catch(Throwable thr){
            out.release();
            throw thr;
        }
    }

    protected WAMPOutputStream numbers(int id, long number1, long number2) throws Throwable{
        WAMPOutputStream out = socket.createOutputStream();
        try{
            JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
            json.writeStartArray();
            json.writeNumber(id);
            json.writeNumber(number1);
            json.writeNumber(number2);
            json.writeEndArray();
            json.close();
            return out;
        }catch(Throwable thr){
            out.release();
            throw thr;
        }
    }

    protected WAMPOutputStream welcomeMessage() throws Throwable{
        if(ROUTER)
            Debugger.temp("<- WelcomeMessage: [%d, %d, %s]", WelcomeMessage.ID, sessionID, Peer.router.details);
        WAMPOutputStream out = socket.createOutputStream();
        try{
            JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
            json.writeStartArray();
            json.writeNumber(WelcomeMessage.ID);
            json.writeNumber(sessionID);
            json.writeTree(Peer.router.details);
            json.writeEndArray();
            json.close();
            return out;
        }catch(Throwable thr){
            out.release();
            throw thr;
        }
    }

    protected WAMPOutputStream registeredMessage(long requestID, long registrationID) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- RegisteredMessage: [%d, %d, %d,...]", RegisteredMessage.ID, requestID, registrationID);
        return numbers(RegisteredMessage.ID, requestID, registrationID);
    }

    protected WAMPOutputStream unregisteredMessage(long requestID) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- UnregisteredMessage: [%d, %d, ...]", UnregisteredMessage.ID, requestID);
        return numbers(UnregisteredMessage.ID, requestID);
    }

    protected WAMPOutputStream invocationMessage(long requestID, long registrationID, ObjectNode details, JsonParser call) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- InvocationMessage: [%d, %d, %s, ...]", InvocationMessage.ID, requestID, registrationID, details);
        WAMPOutputStream out = socket.createOutputStream();
        try{
            JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
            json.writeStartArray();
            json.writeNumber(InvocationMessage.ID);
            json.writeNumber(requestID);
            json.writeNumber(registrationID);
            if(details==null){
                json.writeStartObject();
                json.writeEndObject();
            }else
                json.writeTree(details);
            while(call.nextToken()!=null)
                json.copyCurrentEvent(call);
            json.close();
            return out;
        }catch(Throwable thr){
            out.release();
            throw thr;
        }
    }

    protected WAMPOutputStream resultMessage(long requestID, JsonParser yield) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- ResultMessage: [%d, %d, ...]", ResultMessage.ID, requestID);
        WAMPOutputStream out = socket.createOutputStream();
        try{
            JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
            json.writeStartArray();
            json.writeNumber(ResultMessage.ID);
            json.writeNumber(requestID);
            while(yield.nextToken()!=null)
                json.copyCurrentEvent(yield);
            json.close();
            return out;
        }catch(Throwable thr){
            out.release();
            throw thr;
        }
    }

    protected WAMPOutputStream errorMessage(int requestType, long requestID, ErrorCode errorCode) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- ErrorMessage: [%d, %d, %d, {}, \"%s\", %s, %s]", ErrorMessage.ID, requestType, requestID, errorCode.uri, errorCode.arguments, errorCode.argumentsKw);
        WAMPOutputStream out = socket.createOutputStream();
        try{
            JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
            json.writeStartArray();
            json.writeNumber(ErrorMessage.ID);
            json.writeNumber(requestType);
            json.writeNumber(requestID);
            json.writeStartObject();
            json.writeEndObject();
            json.writeString(errorCode.uri);
            json.writeTree(errorCode.arguments);
            json.writeTree(errorCode.argumentsKw);
            json.writeEndArray();
            json.close();
            return out;
        }catch(Throwable thr){
            out.release();
            throw thr;
        }
    }

    protected WAMPOutputStream errorMessage(int requestType, long requestID, JsonParser error) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- ErrorMessage: [%d, %d, %d, ...]", ErrorMessage.ID, requestType, requestID);
        WAMPOutputStream out = socket.createOutputStream();
        try{
            JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
            json.writeStartArray();
            json.writeNumber(ErrorMessage.ID);
            json.writeNumber(requestType);
            json.writeNumber(requestID);
            while(error.nextToken()!=null)
                json.copyCurrentEvent(error);
            json.close();
            return out;
        }catch(Throwable thr){
            out.release();
            throw thr;
        }
    }

    protected WAMPOutputStream subscribedMessage(long requestID, long subscriptionID) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- SubscribedMessage: [%d, %d, %d, ...]", SubscribedMessage.ID, requestID, subscriptionID);
        return numbers(SubscribedMessage.ID, requestID, subscriptionID);
    }

    protected WAMPOutputStream unsubscribedMessage(long requestID) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- UnsubscribedMessage: [%d, %d, ...]", UnsubscribedMessage.ID, requestID);
        return numbers(UnsubscribedMessage.ID, requestID);
    }

    protected WAMPOutputStream publishedMessage(long requestID, long publicationID) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- PublishedMessage: [%d, %d, %d, ...]", PublishedMessage.ID, requestID, publicationID);
        return numbers(PublishedMessage.ID, requestID, publicationID);
    }

    protected WAMPOutputStream eventMessage(long subscriptionID, long publicationID, ObjectNode options, JsonParser publish) throws Throwable{
        if(ROUTER)
            Debugger.temp("<- EventMessage: [%d, %d, %d, %s, ...]", EventMessage.ID, subscriptionID, publicationID, options);
        WAMPOutputStream out = socket.createOutputStream();
        try{
            JsonGenerator json = serialization.mapper().getFactory().createGenerator(out);
            json.writeStartArray();
            json.writeNumber(EventMessage.ID);
            json.writeNumber(subscriptionID);
            json.writeNumber(publicationID);
            if(options==null){
                json.writeStartObject();
                json.writeEndObject();
            }else
                json.writeTree(options);
            while(publish.nextToken()!=null)
                json.copyCurrentEvent(publish);
            json.close();
            return out;
        }catch(Throwable thr){
            out.release();
            throw thr;
        }
    }
}
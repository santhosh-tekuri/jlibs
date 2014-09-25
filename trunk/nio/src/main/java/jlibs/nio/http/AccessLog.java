/*
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.nio.http;

import jlibs.core.io.FileUtil;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Request;
import jlibs.nio.log.LogRecord;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * @author Santhosh Kumar Tekuri
 */
public class AccessLog{
    private final List<Attribute> attributes = new ArrayList<>();

    public AccessLog(String format){
        StringBuilder buffer = new StringBuilder();

        int i = 0;
        while(i<format.length()){

            // parseConstant
            buffer.setLength(0);
            while(i<format.length()){
                char ch = format.charAt(i);
                if(ch=='$' || ch=='%' || ch=='#'){
                    if(i+1<format.length()){
                        char next = format.charAt(i+1);
                        if(next==ch)
                            ++i;
                        else if(Character.isJavaIdentifierStart(next))
                            break;
                    }
                }
                buffer.append(ch);
                ++i;
            }
            if(buffer.length()>0){
                attributes.add(new Constant(buffer.toString()));
                buffer.setLength(0);
            }

            if(i<format.length()){
                char prefix = format.charAt(i++);
                Class<? extends Exchange> exchangeType = null;
                if(prefix=='$')
                    exchangeType = ServerExchange.class;
                else if(prefix=='%')
                    exchangeType = ClientExchange.class;

                while(i<format.length()){
                    char ch = format.charAt(i);
                    if(Character.isJavaIdentifierPart(ch)){
                        buffer.append(ch);
                        ++i;
                    }else
                        break;
                }
                String name = buffer.toString();
                buffer.setLength(0);
                Function<String, Attribute<?>> creator = registry.get(name);
                if(creator==null){
                    attributes.add(new Constant(prefix+name));
                    continue;
                }

                String arg = null;
                if(i<format.length()){
                    char ch = format.charAt(i);
                    if(ch=='('){
                        ++i;
                        while(i<format.length()){
                            ch = format.charAt(i++);
                            if(ch==')'){
                                arg = buffer.toString();
                                buffer.setLength(0);
                                break;
                            }else
                                buffer.append(ch);
                        }
                    }
                }
                Attribute attr = creator.apply(arg);
                if(attr.exchangeType!=exchangeType)
                    attr = new DelegatingAttribute(exchangeType, attr);
                attributes.add(attr);
            }
        }
    }

    public class Record implements LogRecord{
        private Class<? extends Exchange> owner;
        private String values[] = new String[attributes.size()];

        public void reset(){
            owner = null;
            Arrays.fill(values, null);
        }

        public Class<? extends Exchange> getOwner(){
            return owner;
        }

        public String[] getValues(){
            return values;
        }

        public void process(Exchange exchange, Message msg){
            if(owner==null)
                owner = exchange.getClass();
            for(int i=0; i<values.length; i++){
                Attribute attr = attributes.get(i);
                if(!attr.captureOnFinish && attr.isApplicable(exchange, msg))
                    values[i] = attr.getValueAsString(exchange);
            }
        }

        public void finished(Exchange exchange){
            if(owner==null)
                owner = exchange.getClass();
            for(int i=0; i<values.length; i++){
                Attribute attr = attributes.get(i);
                if(attr.captureOnFinish && attr.isApplicable(exchange)){
                    String value = attr.getValueAsString(exchange);
                    if(value!=null){
                        if(values[i]!=null && !(attr instanceof Constant)){
                            value = Long.toString(Long.parseLong(values[i]) + Long.parseLong(value));
                        }
                    }
                    values[i] = value;
                }
            }
        }

        @Override
        public void publishTo(Appendable writer) throws IOException{
            for(int i=0; i<values.length; i++){
                writer.append(values[i]==null ? "-" : values[i]);
                values[i] = null;
            }
            writer.append(FileUtil.LINE_SEPARATOR);
        }
    }

    /*-------------------------------------------------[ Static Members ]---------------------------------------------------*/

    public static final Map<String, Function<String, Attribute<?>>> registry = new HashMap<>();

    public static void register(Attribute<?> attribute){
        register(attribute.toString(), attribute);
    }

    public static void register(String name, Attribute<?> attribute){
        registry.put(name, s -> attribute);
    }

    public static void register(String name, Function<String, Attribute<?>> builder){
        registry.put(name, builder);
    }

    static{
        register(Exchange.REQUEST_COUNT);
        register(Exchange.SCHEME);
        register(Exchange.HOST);
        register(Exchange.PORT);
        register(Exchange.CONNECTION_STATUS);
        register(Exchange.REQUEST_METHOD);
        register(Exchange.REQUEST_LINE);
        register(Exchange.QUERY_STRING);
        register(Exchange.RESPONSE_STATUS);

        register(ServerExchange.REMOTE_IP);
        register(ServerExchange.CLIENT_IP);

        register("request_header", Exchange.RequestHeader::new);
        register("request_cookie", Exchange.RequestCookie::new);
        register("response_header", Exchange.ResponseHeader::new);
    }

    @SuppressWarnings("unchecked")
    private static class DelegatingAttribute extends Attribute{
        private Attribute delegate;
        public DelegatingAttribute(Class<? extends Exchange> exchangeType, Attribute delegate){
            super(exchangeType, delegate.messageType, delegate.captureOnFinish);
            this.delegate = delegate;
        }

        @Override
        public Object getValue(Exchange exchange){
            return delegate.getValue(exchange);
        }

        @Override
        public String getValueAsString(Exchange exchange){
            return delegate.getValueAsString(exchange);
        }

        @Override
        public String toString(){
            String prefix = "#";
            if(exchangeType==ServerExchange.class)
                prefix = "$";
            else if(exchangeType==ClientExchange.class)
                prefix = "%";
            return prefix+delegate.toString();
        }
    }

    private static class Constant extends Attribute<String>{
        private String value;
        public Constant(String value){
            super(null, Request.class, true);
            this.value = value;
        }

        @Override
        public String getValue(Exchange exchange){
            return value;
        }

        @Override
        public String toString(){
            return value;
        }
    }
}

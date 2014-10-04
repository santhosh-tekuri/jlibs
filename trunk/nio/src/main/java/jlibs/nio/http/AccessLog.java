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
import jlibs.nio.Reactors;
import jlibs.nio.http.expr.Expression;
import jlibs.nio.http.expr.Literal;
import jlibs.nio.http.expr.TypeConversion;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.log.LogHandler;
import jlibs.nio.log.LogRecord;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar Tekuri
 */
public class AccessLog{
    private static final Pattern PATTERN = Pattern.compile("([$%#])\\{(.*?)\\}");
    private static final List<String> REQUEST_VARS = Arrays.asList(
        "request",
        "request_count",
        "scheme",
        "host",
        "port",
        "remote_ip",
        "client_ip",
        "id"
    );

    private static final List<String> CAPTURE_ON_FINISH = Arrays.asList(
        "connection_status"
    );

    private final List<Attribute> attributes = new ArrayList<>();

    public AccessLog(String format) throws ParseException{
        Matcher matcher = PATTERN.matcher(format);
        int cursor = 0;
        while(cursor<format.length() && matcher.find(cursor)){
            String literal = format.substring(cursor, matcher.start());
            if(!literal.isEmpty())
                attributes.add(new Attribute(literal));

            String group1 = matcher.group(1);
            Class exchangeType = null;
            if(group1.equals("$"))
                exchangeType = ServerExchange.class;
            else if(group1.equals("%"))
                exchangeType = ClientExchange.class;

            String group2 = matcher.group(2);
            Class messageType = Response.class;
            if(REQUEST_VARS.contains(group2) || group2.startsWith("request."))
                messageType = Request.class;
            boolean captureOnFinish = CAPTURE_ON_FINISH.contains(group2);
            attributes.add(new Attribute(Expression.compile(group2), exchangeType, messageType, captureOnFinish));

            cursor = matcher.end();
        }
        String literal = format.substring(cursor, format.length());
        if(!literal.isEmpty())
            attributes.add(new Attribute(literal));
    }

    public Reactors.Pool<Record> records = new Reactors.Pool<>(Record::new);
    public class Record implements LogRecord{
        private Class<? extends Exchange> owner;
        private int exchanges = 0;
        private String values[] = new String[attributes.size()];

        private LogHandler logHandler;
        public void setLogHandler(LogHandler logHandler){
            this.logHandler = logHandler;
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
            if(msg instanceof Request)
                ++exchanges;
            for(int i=0; i<values.length; i++){
                Attribute attr = attributes.get(i);
                if(!attr.captureOnFinish && attr.isApplicable(exchange, msg))
                    values[i] = attr.getValue(exchange);
            }
        }

        public void finished(Exchange exchange){
            --exchanges;
            for(int i=0; i<values.length; i++){
                Attribute attr = attributes.get(i);
                if(attr.captureOnFinish && attr.isApplicable(exchange)){
                    String value = attr.getValue(exchange);
                    if(value!=null){
                        if(values[i]!=null)
                            value = Long.toString(Long.parseLong(values[i]) + Long.parseLong(value));
                    }
                    values[i] = value;
                }
            }
            if(exchanges==0){
                logHandler.publish(this);
                reset();
                records.free(this);
            }
        }

        public void reset(){
            owner = null;
            exchanges = 0;
            logHandler = null;
            Arrays.fill(values, null);
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

    private static class Attribute{
        public final Class exchangeType;
        public final Class messageType;
        public final boolean captureOnFinish;
        private Expression expr;
        protected Attribute(Expression expr, Class exchangeType, Class messageType, boolean captureOnFinish){
            this.expr = expr;
            this.exchangeType = exchangeType;
            this.messageType = messageType;
            this.captureOnFinish = captureOnFinish;
        }

        protected Attribute(String literal){
            this(new Literal(literal), null, Request.class, false);
        }

        public String getValue(Exchange exchange){
            return TypeConversion.toString(expr.evaluate(exchange));
        }

        public boolean isApplicable(Exchange exchange){
            return exchangeType==null || exchangeType==exchange.getClass();
        }

        public boolean isApplicable(Exchange exchange, Message message){
            return isApplicable(exchange) && (messageType==null || messageType==message.getClass());
        }

        @Override
        public String toString(){
            if(exchangeType==ServerExchange.class)
                return "${"+expr+'}';
            else if(exchangeType==ClientExchange.class)
                return "%{"+expr+'}';
            else
                return "#{"+expr+'}';
        }
    }
}

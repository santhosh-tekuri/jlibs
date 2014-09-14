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

package jlibs.nio.http.accesslog;

import jlibs.core.io.FileUtil;
import jlibs.nio.http.ClientExchange;
import jlibs.nio.http.Exchange;
import jlibs.nio.http.ServerExchange;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.util.LogHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public abstract class AccessLog{
    private final List<Entry> entries = new ArrayList<>();

    protected AccessLog(Class<? extends Exchange> exchangeType, String format){
        while(!format.isEmpty()){
            format = parseRawString(format);
            if(!format.isEmpty())
                format = parseEntry(exchangeType, format);
        }
    }

    private String parseRawString(String format){
        StringBuilder buffer = new StringBuilder();

        int i = 0;
        while(i<format.length()){
            char ch = format.charAt(i);
            if(ch=='%'){
                ch = format.charAt(i+1);
                if(ch=='%'){
                    buffer.append('%');
                    ++i;
                }else
                    break;
            }else{
                buffer.append(ch);
                ++i;
            }
        }
        if(buffer.length()!=0)
            entries.add(new Entry(buffer.toString()));
        return format.substring(i);
    }

    private String parseEntry(Class<? extends Exchange> exchangeType, String format){
        format = format.substring(1);

        Boolean original = null;
        if(format.startsWith("<")){
            original = true;
            format = format.substring(1);
        }

        String customizer = null;
        if(format.startsWith("{")){
            int end = format.indexOf('}');
            customizer = format.substring(1, end);
            format = format.substring(end+1);
        }

        if(format.startsWith("<")){
            original = true;
            format = format.substring(1);
        }else if(format.startsWith(">")){
            original = false;
            format = format.substring(1);
        }

        String id;
        if(format.startsWith("(")){
            int end = format.indexOf(')');
            id = format.substring(1, end);
            format = format.substring(end+1);
        }else if(format.startsWith("^")){
            id = format.substring(0, 2);
            format = format.substring(2);
        }else{
            id = format.substring(0, 1);
            format = format.substring(1);
        }
        entries.add(new Entry(exchangeType, original, id, customizer));
        return format;
    }

    private static class Entry{
        private final Class exchangeType;
        private final Class msgType;
        final boolean onFinish;
        private Attribute attr;
        public Entry(Class exchangeType, Boolean original, String id, String customizer){
            AttributeRegistry.Entry attrEntry = AttributeRegistry.map.get(id);
            if(attrEntry==null)
                throw new IllegalArgumentException("unknown accesslog attribute: "+id);
            if(original!=null && exchangeType==ServerExchange.class){
                if(attrEntry.msgType==Request.class)
                    exchangeType = original ? ServerExchange.class : ClientExchange.class;
                else if(attrEntry.msgType==Response.class)
                    exchangeType = original ? ClientExchange.class : ServerExchange.class;
            }

            this.exchangeType = attrEntry.bothExchanges ? null : exchangeType;
            this.msgType = attrEntry.msgType;
            onFinish = attrEntry.onFinish;
            attr = attrEntry.creator.create(customizer);
        }

        public Entry(String constant){
            exchangeType = null;
            msgType = null;
            onFinish = true;
            attr = new Constant(constant);
        }

        public String getValue(Exchange exchange){
            return attr.getValue(exchange);
        }

        public boolean matches(Exchange exchange, Message msg){
            return matches(exchange) && (msgType==null || msgType==msg.getClass());
        }

        public boolean matches(Exchange exchange){
            return (exchangeType==null || exchangeType==exchange.getClass());
        }
    }

    private static class Constant implements Attribute{
        private final String value;
        public Constant(String value){
            this.value = value;
        }

        @Override
        public String getValue(Exchange exchange){
            return value;
        }
    }

    public class Record implements LogHandler.Record{
        private String values[] = new String[entries.size()];

        public void reset(){
            Arrays.fill(values, null);
        }

        public void process(Exchange exchange, Message msg){
            for(int i=0; i<values.length; i++){
                Entry entry = entries.get(i);
                if(!entry.onFinish && entry.matches(exchange, msg))
                    values[i] = entry.getValue(exchange);
            }
        }

        public void finished(Exchange exchange){
            for(int i=0; i<values.length; i++){
                Entry entry = entries.get(i);
                if(entry.onFinish && entry.matches(exchange)){
                    String value = entry.getValue(exchange);
                    if(value!=null){
                        if(values[i]==null)
                            values[i] = value;
                        else
                            values[i] = Long.toString(Long.parseLong(values[i]) + Long.parseLong(value));
                    }
                }
            }
        }

        @Override
        public void publish(Appendable writer) throws IOException{
            for(int i=0; i<values.length; i++){
                writer.append(values[i]==null ? "-" : values[i]);
                values[i] = null;
            }
            writer.append(FileUtil.LINE_SEPARATOR);
        }
    }
}

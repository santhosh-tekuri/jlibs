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
import jlibs.nio.Reactor;
import jlibs.nio.http.msg.Request;
import jlibs.nio.util.LogHandler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Santhosh Kumar Tekuri
 */
// https://httpd.apache.org/docs/trunk/mod/mod_log_config.html
// https://httpd.apache.org/docs/trunk/logs.html
public class AccessLog{
    private LogHandler logHandler;
    private List<Entry> entries = new LinkedList<>();

    public void setLogHandler(LogHandler logHandler){
        this.logHandler = logHandler;
    }

    public void add(Entry entry){
        entries.add(entry);
    }

    public int size(){
        return entries.size();
    }

    public class Record implements LogHandler.Record{
        private String values[] = new String[size()];

        public void process(HTTPTask task, jlibs.nio.http.msg.Message msg, Type type){
            Message message = msg instanceof Request ? Message.REQUEST : Message.RESPONSE;
            int index = 0;
            for(Entry entry: entries){
                if(!(entry instanceof FinishEntry) && entry.matches(message, type))
                    values[index] = entry.getValue(task);
                ++index;
            }
        }

        public void taskFinished(HTTPTask task){
            int index = 0;
            for(Entry entry: entries){
                if(entry instanceof FinishEntry){
                    if(entry.message==null && entry.type==null){
                        String value = entry.getValue(task);
                        if(values[index]!=null && value!=null)
                            value = String.valueOf(Long.parseLong(values[index])+Long.parseLong(value));
                        values[index] = value;
                    }else if(task instanceof HTTPServer.Task){
                        if((entry.message==Message.REQUEST && entry.type==Type.ORIGINAL)
                                || (entry.message==Message.RESPONSE && entry.type==Type.FINAL))

                            values[index] = entry.getValue(task);
                    }else{
                        if((entry.message==Message.REQUEST && entry.type==Type.FINAL)
                                || (entry.message==Message.RESPONSE && entry.type==Type.ORIGINAL))

                            values[index] = entry.getValue(task);
                    }
                }
                ++index;
            }
        }

        public void publish(){
            if(logHandler==null){
                synchronized(AccessLog.this){
                    try{
                        publish(System.out);
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
            }else
                logHandler.publish(this);
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


    /*-------------------------------------------------[ Format ]---------------------------------------------------*/

    public void setFormat(boolean server, String format){
        entries.clear();
        while(!format.isEmpty()){
            format = parseRawString(format);
            if(!format.isEmpty())
                format = parseEntry(server, format);
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
            add(constant(buffer.toString()));
        return format.substring(i);
    }

    private String parseEntry(boolean server, String format){
        format = format.substring(1);

        Type type = null;
        if(format.startsWith("<"))
            type = Type.ORIGINAL;

        String name = null;
        if(format.startsWith("{")){
            int end = format.indexOf('}');
            name = format.substring(1, end);
            format = format.substring(end+1);
        }

        if(format.startsWith("<"))
            type = Type.ORIGINAL;
        else if(format.startsWith(">"))
            type = Type.FINAL;
        if(type!=null)
            format = format.substring(1);

        if(format.startsWith("r")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestLine(type));
            format = format.substring(1);
        }else if(format.startsWith("m")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestMethod(type));
            format = format.substring(1);
        }else if(format.startsWith("s")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(responseStatus(type));
            format = format.substring(1);
        }else if(format.startsWith("i")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestHeader(type, name));
            format = format.substring(1);
        }else if(format.startsWith("o")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(responseHeader(type, name));
            format = format.substring(1);
        }else if(format.startsWith("t")){
            boolean begin = true;
            if(name==null)
                name = DEFAULT_DATE_FORMAT;
            else if(name.startsWith("begin:"))
                name = name.substring("begin:".length());
            else if(name.startsWith("end:")){
                name = name.substring("end:".length());
                begin = false;
            }
            if(begin){
                if(type==null)
                    type = server ? Type.ORIGINAL : Type.FINAL;
                add(requestTime(type, name));
            }else{
                if(type==null)
                    type = server ? Type.FINAL : Type.ORIGINAL;
                add(responseTime(type, name));
            }
            format = format.substring(1);
        }else if(format.startsWith("T") || format.startsWith("D")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            TimeUnit timeUnit = format.startsWith("T") ? TimeUnit.SECONDS : TimeUnit.MILLISECONDS;
            add(totalTime(type, timeUnit));
            format = format.substring(1);
        }else if(format.startsWith("H")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestProtocol(type));
            format = format.substring(1);
        }else if(format.startsWith("U")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestURL(type));
            format = format.substring(1);
        }else if(format.startsWith("q")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(queryString(type));
            format = format.substring(1);
        }else if(format.startsWith("B")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(responsePayloadSize(type));
            format = format.substring(1);
        }else if(format.startsWith("I")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestSize(type));
            format = format.substring(1);
        }else if(format.startsWith("O")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(responseSize(type));
            format = format.substring(1);
        }else if(format.startsWith("S")){
            add(transferredSize());
            format = format.substring(1);
        }else if(format.startsWith("X")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(connectionStatus(type));
            format = format.substring(1);
        }else if(format.startsWith("(request.headers)")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestHeaders(type));
            format = format.substring("(request.headers)".length());
        }else if(format.startsWith("(response.headers)")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(responseHeaders(type));
            format = format.substring("(response.headers)".length());
        }else if(format.startsWith("(execution.id)")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(executionID(type));
            format = format.substring("(execution.id)".length());
        }else if(format.startsWith("(request.head.size)")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestHeadSize(type));
            format = format.substring("(request.head.size)".length());
        }else if(format.startsWith("(response.head.size)")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(responseHeadSize(type));
            format = format.substring("(response.head.size)".length());
        }else if(format.startsWith("(request.payload.size)")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestPayloadSize(type));
            format = format.substring("(request.payload.size)".length());
        }else if(format.startsWith("(response.payload.size)")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(responsePayloadSize(type));
            format = format.substring("(response.payload.size)".length());
        }else if(format.startsWith("(request.size)")){
            if(type==null)
                type = server ? Type.ORIGINAL : Type.FINAL;
            add(requestSize(type));
            format = format.substring("(request.size)".length());
        }else if(format.startsWith("(response.size)")){
            if(type==null)
                type = server ? Type.FINAL : Type.ORIGINAL;
            add(responseSize(type));
            format = format.substring("(response.size)".length());
        }else
            throw new IllegalArgumentException(format);

        return format;
    }

    /*-------------------------------------------------[ Entries ]---------------------------------------------------*/

    enum Message{ REQUEST, RESPONSE }
    enum Type{ ORIGINAL, FINAL }

    public static class Entry{
        public final Message message;
        public final Type type;
        private Function<HTTPTask, String> function;
        protected Entry(Message message, Type type, Function<HTTPTask, String> function){
            this.message = message;
            this.type = type;
            this.function = function;
        }

        public String getValue(HTTPTask task){
            return function.apply(task);
        }

        public boolean matches(Message message, Type type){
            if(this.message==null && this.type==null)
                return true;
            else
                return this.message==message && this.type==type;
        }
    }

    public static class FinishEntry extends Entry{
        protected FinishEntry(Message message, Type type, Function<HTTPTask, String> function){
            super(message, type, function);
        }
    }

    public static Entry requestLine(Type type){
        return new Entry(Message.REQUEST, type, task -> {
            Request request = task.getRequest();
            return request.method+" "+request.uri+' '+request.version;
        });
    }

    public static Entry requestMethod(Type type){
        return new Entry(Message.REQUEST, type, task -> task.getRequest().method.name );
    }

    public static Entry responseStatus(Type type){
        return new Entry(Message.RESPONSE, type, task -> String.valueOf(task.getResponse().statusCode) );
    }

    public static Entry requestHeader(Type type, String name){
        return new Entry(Message.REQUEST, type, task -> task.getRequest().headers.value(name) );
    }

    public static Entry responseHeader(Type type, String name){
        return new Entry(Message.RESPONSE, type, task -> task.getResponse().headers.value(name) );
    }

    public static Entry requestHeaders(Type type){
        return new Entry(Message.REQUEST, type, task -> task.getRequest().headers.toString().trim() );
    }

    public static Entry responseHeaders(Type type){
        return new Entry(Message.RESPONSE, type, task -> task.getResponse().headers.toString().trim() );
    }

    public static final String DEFAULT_DATE_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";

    public static Entry requestTime(Type type, String format){
        ThreadLocal<SimpleDateFormat> formatVar = ThreadLocal.withInitial(() -> new SimpleDateFormat(format));
        return new Entry(Message.REQUEST, type, task -> {
            long begin = task.getBeginTime();
            if(begin==-1)
                return null;
            else
                return formatVar.get().format(new Date(begin));
        });
    }

    public static Entry responseTime(Type type, String format){
        ThreadLocal<SimpleDateFormat> formatVar = ThreadLocal.withInitial(() -> new SimpleDateFormat(format));
        return new FinishEntry(Message.RESPONSE, type, task -> {
            long begin = task.getEndTime();
            if(begin==-1)
                return null;
            else
                return formatVar.get().format(new Date(begin));
        });
    }

    public static Entry requestProtocol(Type type){
        return new Entry(Message.REQUEST, type, task -> task.getRequest().version.toString());
    }

    public static Entry requestURL(Type type){
        return new Entry(Message.REQUEST, type, task -> {
            Request request = task.getRequest();
            int question = request.uri.indexOf('?');
            return question==-1 ? request.uri : request.uri.substring(0, question);
        });
    }

    public static Entry queryString(Type type){
        return new Entry(Message.REQUEST, type, task -> {
            Request request = task.getRequest();
            int question = request.uri.indexOf('?');
            return question==-1 ? "" : request.uri.substring(question);
        });
    }

    public static Entry totalTime(Type type, TimeUnit timeUnit){
        return new FinishEntry(Message.RESPONSE, type, task -> {
            long begin = task.getBeginTime();
            long end = task.getEndTime();
            if(begin==-1 || end==-1)
                return null;
            return String.valueOf(timeUnit.convert(end-begin, TimeUnit.MILLISECONDS));
        });
    }

    private static String longValue(long value){
        return value<0 ? null : String.valueOf(value);
    }

    private static String longValue(long value1, long value2){
        if(value1<0 && value2<0)
            return null;
        else if(value1>=0 && value2>=0)
            return longValue(value1+value2);
        else
            return longValue(value1<0 ? value2 : value1);
    }

    public static Entry requestHeadSize(Type type){
        return new Entry(Message.REQUEST, type, task -> longValue(task.getRequestHeadSize()));
    }

    public static Entry responseHeadSize(Type type){
        return new Entry(Message.RESPONSE, type, task -> longValue(task.getResponseHeadSize()));
    }

    public static Entry requestPayloadSize(Type type){
        return new FinishEntry(Message.REQUEST, type, task -> longValue(task.getRequestPayloadSize()));
    }

    public static Entry responsePayloadSize(Type type){
        return new FinishEntry(Message.RESPONSE, type, task -> longValue(task.getResponsePayloadSize()));
    }

    public static Entry requestSize(Type type){
        return new FinishEntry(Message.REQUEST, type, task -> longValue(task.getRequestHeadSize(), task.getRequestPayloadSize()));
    }

    public static Entry responseSize(Type type){
        return new FinishEntry(Message.RESPONSE, type, task -> longValue(task.getResponseHeadSize(), task.getResponsePayloadSize()));
    }

    public static Entry transferredSize(){
        return new FinishEntry(null, null, task -> {
            String req = longValue(task.getRequestHeadSize(), task.getRequestPayloadSize());
            String res = longValue(task.getResponseHeadSize(), task.getResponsePayloadSize());
            if(req==null && res==null)
                return null;
            if(req!=null && res!=null)
                return String.valueOf(Long.parseLong(req)+Long.parseLong(res));
            else
                return req==null ? res : req;
        });
    }

    public static Entry connectionStatus(Type type){
        return new FinishEntry(Message.RESPONSE, type, task -> {
            if(task.getConnectionStatus()==null)
                return null;
            switch(task.getConnectionStatus()){
                case OPEN: return "+";
                case CLOSED: return "-";
                case ABORTED: return "X";
                default: return null;
            }
        });
    }

    public static Entry executionID(Type type){
        return new FinishEntry(Message.RESPONSE, type, task -> Reactor.current().getExecutionID());
    }

    public static Entry constant(String constant){
        return new Entry(null, null, task -> constant);
    }
}

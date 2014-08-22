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

package jlibs.nio.http.msg;

import jlibs.nio.Reactor;
import jlibs.nio.http.msg.spec.*;
import jlibs.nio.util.Bytes;
import jlibs.nio.util.Line;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Headers implements Line.Consumer, Encodable, Bytes.Encodable{
    private final TreeMap<String, Header> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Header head;

    private boolean validateLinks(){
        Header h1 = head;
        int count1 = 0;
        while(h1!=null){
            if(h1.next==null)
                assert head.prev==h1;
            else
                assert h1.next.prev==h1;
            h1 = h1.next;
            ++count1;
        }

        int count2 = 0;
        for(Header h2: map.values()){
            Header t = h2;
            while(h2!=null){
                if(h2.sameNext==null)
                    assert t.samePrev==h2;
                else
                    assert h2.sameNext.samePrev==h2;
                h2 = h2.sameNext;
                ++count2;
            }
        }

        return count1==count2;
    }

    /*-------------------------------------------------[ Get ]---------------------------------------------------*/

    public Header getFirst(){
        return head;
    }

    public Header get(HeaderSpec spec){
        return get(spec.name);
    }

    public Header get(String name){
        return map.get(name);
    }

    public String value(HeaderSpec spec){
        return value(spec.name);
    }

    public String value(String name){
        Header header = map.get(name);
        return header==null ? null : header.value;
    }


    /*-------------------------------------------------[ Remove ]---------------------------------------------------*/

    public void remove(Header header){
        if(header==head){
            head = header.next;
            if(head!=null)
                head.prev = header.prev;
        }else{
            header.prev.next = header.next;
            if(header.next==null)
                head.prev = header.prev;
            else
                header.next.prev = header.prev;
        }
        header.prev = header;
        header.next = null;

        if(header.samePrev.sameNext==null){
            Header head = header.sameNext;
            if(head==null)
                map.remove(header.name);
            else{
                map.put(header.name, head);
                head.samePrev = header.samePrev;
            }
        }else{
            header.samePrev.sameNext = header.sameNext;
            if(header.sameNext!=null)
                header.sameNext.samePrev = header.samePrev;
        }
        header.samePrev = header;
        header.sameNext = null;

        assert validateLinks();
    }

    public Header remove(HeaderSpec spec){
        return remove(spec.name);
    }

    private void removeSameNext(Header header){
        while(header!=null){
            Header sameNext = header.sameNext;

            if(header==head){
                head = header.next;
                if(head!=null)
                    head.prev = header.prev;
            }else{
                header.prev.next = header.next;
                if(header.next!=null)
                    header.next.prev = header.prev;
            }
            header.prev = header;
            header.next = null;

            header = sameNext;

            assert validateLinks();
        }
    }

    public Header remove(String name){
        Header removed = map.get(name);
        if(removed!=null){
            remove(removed);
            removeSameNext(removed);
        }
        return removed;
    }

    public void clear(){
        map.clear();
        head = null;
    }

    /*-------------------------------------------------[ Add ]---------------------------------------------------*/

    private Header newHeader(String name, String value){
        Header header = new Header(name, value);
        if(head==null)
            head = header;
        else{
            header.prev = head.prev;
            head.prev.next = header;
            head.prev = header;
        }
        return header;
    }

    public void set(HeaderSpec spec, String value){
        set(spec.name, value);
    }

    public void set(String name, String value){
        if(value==null){
            remove(name);
            return;
        }
        Header header = map.get(name);
        if(header==null)
            map.put(name, newHeader(name, value));
        else{
            header.value = value;
            removeSameNext(header.sameNext);
            header.samePrev = header;
        }
        assert validateLinks();
    }

    public void add(HeaderSpec spec, String value){
        add(spec.name, value);
    }

    public void add(String name, String value){
        if(value==null)
            return;
        Header header = newHeader(name, value);
        Header head = map.get(name);
        if(head==null)
            map.put(name, header);
        else{
            header.samePrev = head.samePrev;
            head.samePrev.sameNext = header;
            head.samePrev = header;
        }
        assert validateLinks();
    }

    /*-------------------------------------------------[ Unmarshalling ]---------------------------------------------------*/

    private String name;
    private String value;
    boolean populated;
    private List<String> trailers;

    @Override
    public void consume(Line line){
        if(line.length()==0){
            if(name!=null){
                add(name, value);
                name = value = null;
            }
            if(populated){
                if(trailers!=null)
                    remove(TRAILER.name);
                trailers = null;
            }
            populated = true;
            return;
        }else if(name!=null){
            if(line.charAt(0)==' '||line.charAt(0)=='\t'){
                int valueBegin = line.indexOf(false, 1);
                if(valueBegin==-1)
                    value += ' ';
                else{
                    int valueEnd = line.indexOf(false, -(line.length()-1));
                    value += ' '+line.substring(valueBegin, valueEnd+1);
                }
                return;
            }else{
                add(name, value);
                name = value = null;
            }
        }

        int nameBegin = line.indexOf(false, 0);
        int colon = line.indexOf(':', nameBegin);
        int nameEnd = line.indexOf(false, -(colon-1));
        name = line.substring(nameBegin, nameEnd+1);//, headerNames);
        if(populated){
            if(trailers==null){
                trailers = TRAILER.parse(value, Version.HTTP_1_1);
                for(String token: trailers){
                    if(token.equalsIgnoreCase(TRANSFER_ENCODING.name)
                            || token.equalsIgnoreCase(CONTENT_LENGTH.name)
                            || token.equalsIgnoreCase(TRAILER.name))
                        throw new IllegalArgumentException("Unacceptable Trailer Token");
                }
            }
            boolean acceptable = false;
            for(String trailer: trailers){
                if(name.equalsIgnoreCase(trailer)){
                    acceptable = true;
                    break;
                }
            }
            if(!acceptable)
                throw new IllegalArgumentException("Unacceptable Trailer");
        }

        int valueBegin = line.indexOf(false, colon+1);
        if(valueBegin==-1)
            value = "";
        else{
            int valueEnd = line.indexOf(false, -(line.length()-1));
            value = line.substring(valueBegin, valueEnd+1);
        }
    }

    /*-------------------------------------------------[ Marshalling ]---------------------------------------------------*/

    @Override
    public String toString(){
        StringBuilder buffer = new StringBuilder();
        Header header = head;
        while(header!=null){
            buffer.append(header.name).append(": ").append(header.value).append("\r\n");
            header = header.next;
        }
        buffer.append("\r\n");
        return buffer.toString();
    }

    public ByteBuffer encode(Bytes bytes, ByteBuffer buffer){
        Header header = head;
        while(header!=null){
            buffer = bytes.append(header.name, buffer);
            buffer = bytes.append(": ", buffer);
            buffer = bytes.append(header.value, buffer);
            buffer = bytes.append("\r\n", buffer);
            header = header.next;
        }
        return bytes.append("\r\n", buffer);
    }

    @Override
    public Bytes encodeTo(Bytes bytes){
        ByteBuffer buffer = Reactor.current().bufferPool.borrow(Bytes.CHUNK_SIZE);
        buffer = encode(bytes, buffer);
        buffer.flip();
        bytes.append(buffer);
        return bytes;
    }

    @Override
    public void encodeTo(OutputStream out) throws IOException{
        Header header = head;
        while(header!=null){
            header.encodeTo(out);
            header = header.next;
        }
        out.write('\r');
        out.write('\n');
    }

    /*-------------------------------------------------[ Standard Headers ]---------------------------------------------------*/

    public static final Connection CONNECTION = new Connection("Connection");
    public static final Connection PROXY_CONNECTION = new Connection("Proxy-Connection");
    public static final Host HOST = new Host();

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13
    public static final HTTPLong CONTENT_LENGTH = new HTTPLong("Content-Length");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11
    public static final HTTPEncoding CONTENT_ENCODING = new HTTPEncoding("Content-Encoding");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.41
    public static final HTTPEncoding TRANSFER_ENCODING = new HTTPEncoding("Transfer-Encoding");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.18
    public static final HTTPDate DATE = new HTTPDate("Date");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.6
    public static final HTTPLong AGE = new HTTPLong("Age");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21
    public static final HTTPDate EXPIRES = new HTTPDate("Expires");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.25
    public static final HTTPDate IF_MODIFIED_SINCE = new HTTPDate("If-Modified-Since");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.28
    public static final HTTPDate IF_UNMODIFIED_SINCE = new HTTPDate("If-Unmodified-Since");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.29
    public static final HTTPDate LAST_MODIFIED = new HTTPDate("Last-Modified");

    public static final Authorization AUTHORIZATION = new Authorization("Authorization");
    public static final Authorization PROXY_AUTHORIZATION = new Authorization("Proxy-Authorization");

    public static final Authenticate WWW_AUTHENTICATE = new Authenticate("WWW-Authenticate");
    public static final Authenticate PROXY_AUTHENTICATE = new Authenticate("Proxy-Authenticate");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17
    public static final ContentType CONTENT_TYPE = new ContentType();

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
    public static final Accept ACCEPT = new Accept();

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
    public static final AcceptCharset ACCEPT_CHARSET = new AcceptCharset();

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.7
    public static final Allow ALLOW = new Allow();

    // http://tools.ietf.org/html/rfc6265#section-4.2
    public static final CookieSpec COOKIE = new CookieSpec();

    // http://tools.ietf.org/html/rfc6265#section-4.1
    public static final SetCookie SET_COOKIE = new SetCookie();

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.36
    public static final StringValueHeader REFERER = new StringValueHeader("Referer");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30
    public static final StringValueHeader LOCATION = new StringValueHeader("Location");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30
    public static final StringValueHeader CONTENT_LOCATION = new StringValueHeader("Content-Location");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.20
    public static final ExpectSpec EXPECT = new ExpectSpec();

    // http://en.wikipedia.org/wiki/X-Forwarded-For
    public static final StringListHeader X_FORWARDED_FOR = new StringListHeader("X-Forwarded-For");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.40
    public static final StringListHeader TRAILER = new StringListHeader("Trailer");

    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1
    public static final ContentDispositionSpec CONTENT_DISPOSITION = new ContentDispositionSpec();

    // http://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383528
    public static final StringValueHeader SOAP_ACTION = new StringValueHeader("SOAPAction");
}

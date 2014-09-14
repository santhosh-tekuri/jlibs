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

import jlibs.nio.http.ConnectionStatus;
import jlibs.nio.http.ServerExchange;
import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;
import jlibs.nio.http.util.Cookie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class AttributeRegistry{
    public static final Map<String, Entry> map = new HashMap<>();

    public static void register(String name, Class<? extends Message> msgType, boolean bothExchanges, boolean onFinish, AttributeCreator creator){
        map.put(name, new Entry(msgType, bothExchanges, onFinish, creator));
    }

    public static class Entry{
        Class<? extends Message> msgType;
        boolean bothExchanges;
        boolean onFinish;
        AttributeCreator creator;
        public Entry(Class<? extends Message> msgType, boolean bothExchanges, boolean onFinish, AttributeCreator creator){
            this.msgType = msgType;
            this.bothExchanges = bothExchanges;
            this.onFinish = onFinish;
            this.creator = creator;
        }
    }

    static{
        // %X => Connection status when response is completed:
        //       X = Connection aborted before the response completed
        //       + = Connection kept alive after the response completed.
        //       - = Connection closed after the response completed.
        register("X", Response.class, false, true, customizer -> {
            return exchange -> {
                ConnectionStatus conStatus = exchange.getConnectionStatus();
                if(conStatus!=null){
                    switch(conStatus){
                        case OPEN: return "+";
                        case CLOSED: return "-";
                        case ABORTED: return "X";
                    }
                }
                return null;
            };
        });

        // %v => server name
        register("s", Request.class, false, false, customizer -> {
            return exchange -> {
                return exchange.getEndpoint().host;
            };
        });

        // %p => port
        register("p", Request.class, false, false, customizer -> {
            return exchange -> {
                return Integer.toString(exchange.getEndpoint().port);
            };
        });

        // %m => The request method
        register("m", Request.class, false, false, customizer -> {
            return exchange -> {
                return exchange.getRequest().method.name;
            };
        });

        // %U => The URL path requested, not including any query string
        register("U", Request.class, false, false, customizer -> {
            return exchange -> {
                Request request = exchange.getRequest();
                int question = request.uri.indexOf('?');
                return question==-1 ? request.uri : request.uri.substring(0, question);
            };
        });

        // %q => The query string (prepended with a ? if a query string exists, otherwise an empty string)
        register("q", Request.class, false, false, customizer -> {
            return exchange -> {
                Request request = exchange.getRequest();
                int question = request.uri.indexOf('?');
                return question==-1 ? "" : request.uri.substring(question);
            };
        });

        // %H => request protocol
        register("H", Request.class, false, false, customizer -> {
            return exchange -> {
                return exchange.getRequest().version.text;
            };
        });

        // %r => First line of request
        register("r", Request.class, false, false, customizer -> {
            return exchange -> {
                Request request = exchange.getRequest();
                return request.method+" "+request.uri+' '+request.version;
            };
        });

        // %{HEADER_NAME}i => request header value
        register("i", Request.class, false, false, headerName -> {
            return exchange -> {
                return exchange.getRequest().headers.value(headerName);
            };
        });

        // %(request.headers) => request headers
        register("request.headers", Request.class, false, false, customizer -> {
            return exchange -> {
                return exchange.getRequest().headers.toString().trim();
            };
        });

        // %{COOKIE_NAME}C => cookie value
        register("C", Request.class, false, false, cookieName -> {
            return exchange -> {
                Cookie cookie = exchange.getRequest().getCookies().get(cookieName);
                return cookie==null ? null : cookie.value;
            };
        });

        // %h => remote ip address
        register("h", Request.class, false, false, customizer -> {
            return exchange -> {
                return ((ServerExchange)exchange).getClientAddress().getHostAddress();
            };
        });

        // %a => Replaces the original client IP address for the connection with
        //       the useragent IP address list presented by a proxies or a load
        //       balancer via the request headers
        register("a", Request.class, false, false, customizer -> {
            return exchange -> {
                List<String> list = exchange.getRequest().getXForwardedFor();
                if(!list.isEmpty())
                    return list.get(0);
                return ((ServerExchange)exchange).getClientAddress().getHostAddress();
            };
        });


        // %s => response status
        register("s", Response.class, false, false, customizer -> {
            return exchange -> {
                return Integer.toString(exchange.getResponse().status.code);
            };
        });

        // %{HEADER_NAME}o => response header value
        register("o", Request.class, false, false, headerName -> {
            return exchange -> {
                return exchange.getResponse().headers.value(headerName);
            };
        });

        // %(response.headers) => response headers
        register("response.headers", Request.class, false, false, customizer -> {
            return exchange -> {
                return exchange.getResponse().headers.toString().trim();
            };
        });
    }
}

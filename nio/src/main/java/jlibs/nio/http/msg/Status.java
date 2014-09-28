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

import jlibs.nio.http.expr.Bean;
import jlibs.nio.http.expr.UnresolvedException;
import jlibs.nio.http.util.USAscii;

import java.nio.ByteBuffer;

import static jlibs.nio.http.util.USAscii.*;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Status extends RuntimeException implements Bean{
    public final int code;
    public final String reason;
    public final boolean payloadNotAllowed;

    private final byte bytes10[];
    private final byte bytes11[];
    private final byte bytesxx[];

    public Status(int code, String reason){
        this(code, reason, false);
    }

    private Status(int code, String reason, boolean createBytes){
        this.code = code;
        this.reason = reason;

        if(createBytes){
            bytes10 = USAscii.toBytes("HTTP/1.0 "+code+" "+reason+"\r\n");
            bytes11 = USAscii.toBytes("HTTP/1.1 "+code+" "+reason+"\r\n");
            bytesxx = USAscii.toBytes(" "+code+" "+reason+"\r\n");
            array[code-100] = this;
        }else
            bytes10 = bytes11 = bytesxx = null;

        payloadNotAllowed = (code/100)==1 // 1xx: Informational
                        || code==204      // NO_CONTENT
                        || code==205      // RESET_CONTENT
                        || code==304;     // NOT_MODIFIED
    }

    @Override
    public String getMessage(){
        return reason;
    }

    @Override
    public Throwable fillInStackTrace(){
        return this;
    }

    public Status with(String msg){
        assert msg!=null;
        return new Status(code, msg, false);
    }

    public Status with(Throwable thr){
        assert thr!=null;
        if(thr instanceof Status)
            return (Status)thr;
        String msg  = thr.getMessage();
        if(msg==null)
            msg = thr.getClass().getSimpleName();
        Status status = new Status(code, msg, false);
        status.initCause(thr);
        return status;
    }

    public Status with(String msg, Throwable thr){
        if(thr instanceof Status)
            return ((Status)thr).with(msg);
        Status status = new Status(code, msg, false);
        status.initCause(thr);
        return status;
    }

    public void putInto(ByteBuffer buffer, Version version){
        if(version==Version.HTTP_1_1 && bytes11!=null)
            buffer.put(bytes11, 0, bytes11.length);
        else if(version==Version.HTTP_1_0 && bytes10!=null)
            buffer.put(bytes10, 0, bytes10.length);
        else if(bytesxx!=null){
            version.putInto(buffer);
            buffer.put(bytesxx, 0, bytesxx.length);
        }else{
            version.putInto(buffer);
            buffer.put(SP);
            buffer.put((byte)(code/100+'0'));
            buffer.put((byte)(code/10%10+'0'));
            buffer.put((byte)(code%10+'0'));
            buffer.put(SP);
            USAscii.append(buffer, reason);
            buffer.put(CR);
            buffer.put(LF);
        }
    }

    public boolean isInformational(){ return code/100==1; }
    public boolean isSuccessful(){ return code/100==2; }
    public boolean isRedirection(){ return code/100==3; }
    public boolean isClientError(){ return code/100==4; }
    public boolean isServerError(){ return code/100==5; }
    public boolean isError(){ return isClientError() || isServerError(); }

    @Override
    public int hashCode(){
        return code;
    }

    @Override
    public boolean equals(Object obj){
        return obj==this || (obj instanceof Status && this.code==(((Status)obj).code));
    }

    @Override
    public String toString(){
        return code+" "+reason;
    }

    @Override
    @SuppressWarnings("StringEquality")
    public Object getField(String name) throws UnresolvedException{
        if(name=="code")
            return code;
        else if(name=="reason")
            return reason;
        else
            throw new UnresolvedException(name);
    }

    private static final Status array[] = new Status[900];

    // 1xx: Informational
    public static final Status CONTINUE            = new Status(100, "Continue", true);
    public static final Status SWITCHING_PROTOCOLS = new Status(101, "Switching Protocols", true);
    public static final Status PROCESSING          = new Status(102, "Processing", true);

    // 2xx: Successful
    public static final Status OK                            = new Status(200, "OK", true);
    public static final Status CREATED                       = new Status(201, "Created", true);
    public static final Status ACCEPTED                      = new Status(202, "Accepted", true);
    public static final Status NON_AUTHORITATIVE_INFORMATION = new Status(203, "Non-Authoritative Information", true);
    public static final Status NO_CONTENT                    = new Status(204, "No Content", true);
    public static final Status RESET_CONTENT                 = new Status(205, "Reset Content", true);
    public static final Status PARTIAL_CONTENT               = new Status(206, "Partial Content", true);
    public static final Status MULTI_STATUS                  = new Status(207, "Multi-Status", true);
    public static final Status ALREADY_REPORTED              = new Status(208, "Already Reported", true);
    public static final Status IM_USED                       = new Status(226, "IM Used", true);

    // 3xx: Redirection
    public static final Status MULTIPLE_CHOICES   = new Status(300, "Multiple Choices", true);
    public static final Status MOVED_PERMANENTLY  = new Status(301, "Moved Permanently", true);
    public static final Status FOUND              = new Status(302, "Found", true);
    public static final Status SEE_OTHER          = new Status(303, "See Other", true);
    public static final Status NOT_MODIFIED       = new Status(304, "Not Modified", true);
    public static final Status USE_PROXY          = new Status(305, "Use Proxy", true);
    public static final Status SWITCH_PROXY       = new Status(306, "Switch Proxy", true);
    public static final Status TEMPORARY_REDIRECT = new Status(307, "Temporary Redirect", true);
    public static final Status PERMANENT_REDIRECT = new Status(308, "Permanent Redirect", true);

    // 4xx: Client Error
    public static final Status BAD_REQUEST                     = new Status(400, "Bad Request", true);
    public static final Status UNAUTHORIZED                    = new Status(401, "Unauthorized", true);
    public static final Status PAYMENT_REQUIRED                = new Status(402, "Payment Required", true);
    public static final Status FORBIDDEN                       = new Status(403, "Forbidden", true);
    public static final Status NOT_FOUND                       = new Status(404, "Not Found", true);
    public static final Status METHOD_NOT_ALLOWED              = new Status(405, "Method Not Allowed", true);
    public static final Status NOT_ACCEPTABLE                  = new Status(406, "Not Acceptable", true);
    public static final Status PROXY_AUTHENTICATION_REQUIRED   = new Status(407, "Proxy Authentication Required", true);
    public static final Status REQUEST_TIMEOUT                 = new Status(408, "Request Timeout", true);
    public static final Status CONFLICT                        = new Status(409, "Conflict", true);
    public static final Status GONE                            = new Status(410, "Gone", true);
    public static final Status LENGTH_REQUIRED                 = new Status(411, "Length Required", true);
    public static final Status PRECONDITION_FAILED             = new Status(412, "Precondition Failed", true);
    public static final Status REQUEST_ENTITY_TOO_LARGE        = new Status(413, "Request Entity Too Large", true);
    public static final Status REQUEST_URI_TOO_LONG            = new Status(414, "Request-URI Too Long", true);
    public static final Status UNSUPPORTED_MEDIA_TYPE          = new Status(415, "Unsupported Media Type", true);
    public static final Status REQUESTED_RANGE_NOT_SATISFIABLE = new Status(416, "Requested Range Not Satisfiable", true);
    public static final Status EXPECTATION_FAILED              = new Status(417, "Expectation Failed", true);
    public static final Status IM_A_TEAPOT                     = new Status(418, "I'm a teapot", true);
    public static final Status AUTHENTICATION_TIMEOUT          = new Status(419, "Authentication Timeout", true);
    public static final Status METHOD_FAILURE                  = new Status(420, "Method Failure", true);
    public static final Status UNPROCESSABLE_ENTITY            = new Status(422, "Unprocessable Entity", true);
    public static final Status LOCKED                          = new Status(423, "Locked", true);
    public static final Status FAILED_DEPENDENCY               = new Status(424, "Failed Dependency", true);
    public static final Status UPGRADE_REQUIRED                = new Status(426, "Upgrade Required", true);
    public static final Status PRECONDITION_REQUIRED           = new Status(428, "Precondition Required", true);
    public static final Status TOO_MANY_REQUESTS               = new Status(429, "Too Many Requests", true);
    public static final Status REQUEST_HEADER_FIELDS_TOO_LARGE = new Status(431, "Request Header Fields Too Large", true);

    // 5xx: Server Error
    public static final Status INTERNAL_SERVER_ERROR           = new Status(500, "Internal Server Error", true);
    public static final Status NOT_IMPLEMENTED                 = new Status(501, "Not Implemented", true);
    public static final Status BAD_GATEWAY                     = new Status(502, "Bad Gateway", true);
    public static final Status SERVICE_UNAVAILABLE             = new Status(503, "Service Unavailable", true);
    public static final Status GATEWAY_TIMEOUT                 = new Status(504, "Gateway Timeout", true);
    public static final Status HTTP_VERSION_NOT_SUPPORTED      = new Status(505, "HTTP Version Not Supported", true);
    public static final Status INSUFFICIENT_STORAGE            = new Status(507, "Insufficient Storage", true);
    public static final Status LOOP_DETECTED                   = new Status(508, "Loop Detected", true);
    public static final Status NOT_EXTENDED                    = new Status(510, "Not Extended", true);
    public static final Status NETWORK_AUTHENTICATION_REQUIRED = new Status(511, "Network Authentication Required", true);

    public static Status valueOf(int code, CharSequence seq){
        if(code<100 || code>999)
            throw new IllegalArgumentException("bad status code: "+code);
        Status status = array[code-100];
        if(status==null || !status.reason.contentEquals(seq))
            return new Status(code, seq.toString(), false);
        return status;
    }
}


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

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Status{
    private Status(){}

    private static String[] defaultMessages = {
        null,
        "Informational",
        "Successful",
        "Redirection",
        "Client Error",
        "Server Error"
    };

    private static String[][] messages = {
        {},
        // 1xx: Informational
        {
            "Continue",
            "Switching Protocols",
            "Processing",
        },
        // 2xx: Successful
        {
            "OK",
            "Created",
            "Accepted",
            "Non-Authoritative Information",
            "No Content",
            "Reset Content",
            "Partial Content",
            "Multi-Status",
            "Already Reported",
        },
        // 3xx: Redirection
        {
            "Multiple Choices",
            "Moved Permanently",
            "Found",
            "See Other",
            "Not Modified",
            "Use Proxy",
            "Switch Proxy",
            "Temporary Redirect",
            "Permanent Redirect",
        },
        // 4xx: Client Error
        {
            "Bad Request",
            "Unauthorized",
            "Payment Required",
            "Forbidden",
            "Not Found",
            "Method Not Allowed",
            "Not Acceptable",
            "Proxy Authentication Required",
            "Request Timeout",
            "Conflict",
            "Gone",
            "Length Required",
            "Precondition Failed",
            "Request Entity Too Large",
            "Request-URI Too Long",
            "Unsupported Media Type",
            "Requested Range Not Satisfiable",
            "Expectation Failed",
            "I'm a teapot",
            "Authentication Timeout",
            "Method Failure",
            null,
            "Unprocessable Entity",
            "Locked",
            "Failed Dependency",
            null,
            "Upgrade Required",
            null,
            "Precondition Required",
            "Too Many Requests",
            null,
            "Request Header Fields Too Large"
        },
        // 5xx: Server Error
        {
            "Internal Server Error",
            "Not Implemented",
            "Bad Gateway",
            "Service Unavailable",
            "Gateway Timeout",
            "HTTP Version Not Supported",
        }
    };

    // 1xx: Informational
    public static final int CONTINUE = 100;
    public static final int SWITCHING_PROTOCOLS = 101;
    public static final int PROCESSING = 102;

    // 2xx: Successful
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int NO_CONTENT = 204;
    public static final int RESET_CONTENT = 205;
    public static final int PARTIAL_CONTENT = 206;
    public static final int MULTI_STATUS = 207;
    public static final int ALREADY_REPORTED = 208;

    // 3xx: Redirection
    public static final int MULTIPLE_CHOICES = 300;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int FOUND = 302;
    public static final int SEE_OTHER = 303;
    public static final int NOT_MODIFIED = 304;
    public static final int USE_PROXY = 305;
    public static final int SWITCH_PROXY = 306;
    public static final int TEMPORARY_REDIRECT = 307;
    public static final int PERMANENT_REDIRECT = 308;

    // 4xx: Client Error
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int PRECONDITION_FAILED = 412;
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;
    public static final int REQUEST_URI_TOO_LONG = 414;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static final int EXPECTATION_FAILED = 417;
    public static final int IM_A_TEAPOT = 418;
    public static final int AUTHENTICATION_TIMEOUT = 419;
    public static final int METHOD_FAILURE = 420;
    public static final int UNPROCESSABLE_ENTITY = 422;
    public static final int LOCKED = 423;
    public static final int FAILED_DEPENDENCY = 424;
    public static final int UPGRADE_REQUIRED = 426;
    public static final int PRECONDITION_REQUIRED = 428;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

    // 5xx: Server Error
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

    // dont use as response code
    public static final int BAD_RESPONSE = 600;
    public static final int RESPONSE_TIMEOUT = 601;
    public static final int RESPONSE_LINE_TOO_LONG = 602;
    public static final int RESOPNSE_HEADER_FIELDS_TOO_LARGE = 603;

    public static String message(int code){
        int category = code/100;
        if(category<messages.length){
            String msgs[] = messages[category];
            int index = code%100;
            if(index<msgs.length && msgs[index]!=null)
                return msgs[index];
            else
                return defaultMessages[category];
        }else
            return null;
    }

    public static String message(int code, String message){
        return message==null ? message(code) : message;
    }

    public static boolean isPayloadNotAllowed(int code){
        return code==NO_CONTENT
                || code==RESET_CONTENT
                || code==NOT_MODIFIED
                || (code/100)==1; // 1xx: Informational
    }

    public static boolean isInformational(int code){
        return code/100==1;
    }

    public static boolean isSuccessful(int code){
        return code/100==2;
    }

    public static boolean isRedirection(int code){
        return code/100==3;
    }

    public static boolean isClientError(int code){
        return code/100==4;
    }

    public static boolean isServerError(int code){
        return code/100==5;
    }

    public static boolean isError(int code){
        return isClientError(code) || isServerError(code);
    }
}

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

import jlibs.nio.*;
import jlibs.nio.async.ExecutionContext;
import jlibs.nio.http.msg.Request;
import jlibs.nio.http.msg.Response;

import java.io.Closeable;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface HTTPTask<T extends HTTPTask> extends Closeable, Attachable{
    public void resume();
    public default void resume(Throwable thr){
        resume(-1, null, thr);
    }

    public default void resume(int errorCode){
        resume(errorCode, null, null);
    }

    public default void resume(int errorCode, String reasonPhrase){
        resume(errorCode, reasonPhrase, null);
    }

    public default void resume(int errorCode, Throwable thr){
        resume(errorCode, null, thr);
    }

    public void resume(int errorCode, String reasonPhrase, Throwable thr);

    public boolean isOpen();

    public long getBeginTime();
    public Reactor getReactor();
    public ClientEndpoint getClientEndpoint();
    public Request getRequest();
    public Response getResponse();
    public void finish(FinishListener<? super T> listener);
    public boolean isSuccess();
    public Throwable getError();
    public int getErrorCode();
    public String getErrorPhrase();
    public long getEndTime();
    public Client stealClient();
    public ConnectionStatus getConnectionStatus();

    public long getRequestHeadSize();
    public long getResponseHeadSize();
    public long getRequestPayloadSize();
    public long getResponsePayloadSize();

    public static interface FinishListener<T extends HTTPTask>{
        public static final FinishListener<HTTPTask> DO_NOTHING = task -> {};
        public void finished(T task);
    }

    public static interface Filter<T extends HTTPTask>{
        public void filter(T task) throws Exception;
    }
    public static interface RequestFilter<T extends HTTPTask> extends Filter<T>{}
    public static interface ResponseFilter<T extends HTTPTask> extends Filter<T>{}
}

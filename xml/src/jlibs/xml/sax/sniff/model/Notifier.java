/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.sniff.model;

import jlibs.xml.sax.sniff.Debuggable;
import jlibs.xml.sax.sniff.engine.context.Context;

/**
 * @author Santhosh Kumar T
 */
public abstract class Notifier implements Debuggable{
    public HitManager hits = new HitManager();
    public int depth;

    public abstract Datatype resultType();
    public abstract void addNotificationListener(NotificationListener listener);
    public abstract void notify(Context context, Object result);
}

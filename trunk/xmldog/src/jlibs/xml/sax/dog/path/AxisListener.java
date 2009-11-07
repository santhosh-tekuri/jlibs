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

package jlibs.xml.sax.dog.path;

import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.LinkableEvaluation;

/**
 * @author Santhosh Kumar T
 */
public abstract class AxisListener<X extends Expression> extends LinkableEvaluation<X>{
    protected AxisListener(X expression, long order){
        super(expression, order);
    }

    public abstract void onHit(EventID eventID);
    public abstract void expired();


    public boolean manuallyExpired;
    public AxisListener nextAxisListener;
}

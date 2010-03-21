/**
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

package jlibs.xml.sax.dog.expr;

/**
 * @author Santhosh Kumar T
 */
public abstract class Evaluation<X extends Expression> extends EvaluationListener{
    public final X expression;
    public final long order;

    protected Evaluation(X expression, long order){
        this.expression = expression;
        this.order = order;
    }

    public abstract void start();
    public abstract Object getResult();

    /*-------------------------------------------------[ Listener Support ]---------------------------------------------------*/

    protected EvaluationListener listener;

    public void addListener(EvaluationListener listener){
        this.listener = listener;
    }

    public void removeListener(EvaluationListener listener){
        if(this.listener==listener){
            this.listener = null;
            dispose();
        }
    }

    protected void fireFinished(){
        listener.finished(this);
    }

    protected void dispose(){}
}

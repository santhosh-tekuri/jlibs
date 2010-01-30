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

package jlibs.xml.sax.dog.expr;

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * The root interface in expression hierarchy.
 *
 * @author Santhosh Kumar T
 */
public abstract class Expression{
    /**
     * Only doc scope expression have this filed set.
     * All doc level expression in a xmldog instance are
     * assigned unique number sequentially.
     *
     * @see jlibs.xml.sax.dog.XMLDog#addXPath(String)
     * assigns unique sequential id.
     *
     * this id used by:
     *
     * @see jlibs.xml.sax.dog.sniff.Event#listeners
     * array to manage listeners for expression
     *
     * @see jlibs.xml.sax.dog.sniff.Event#results
     * array to manage result for expression
     *
     * @performance
     *      this is introduced to use list instead of map to
     * manage listeners and results by Event for doc scope
     * expressions
     */
    public int id;
    public final DataType resultType;
    protected int scope;

    public Expression(int scope, DataType resultType){
        this.scope = scope;
        this.resultType = resultType;
    }

    /** @see jlibs.xml.sax.dog.Scope */
    public final int scope(){
        return scope;
    }

    /*-------------------------------------------------[ Result ]---------------------------------------------------*/

    /** This method is called only glocal scope expression */
    public abstract Object getResult();

    /**
     * This method is called only non-glocal scope expression.
     * it can return the result if it is possible, otherwize
     * returns Evaluation object.
     */
    public abstract Object getResult(Event event);

    /*-------------------------------------------------[ Simplification ]---------------------------------------------------*/

    /**
     * returns simplified expression which peforms better.
     * if no simpliciation possible it simply returns current expression.
     *
     * default implementation promotes global-scoped expressions to literal
     */
    public Expression simplify(){
        return scope==Scope.GLOBAL ? new Literal(getResult(), resultType) : this;
    }

    /*-------------------------------------------------[ XPath ]---------------------------------------------------*/

    /**
     * only user given have xpath set.
     */
    protected String xpath;

    public final void setXPath(String xpath){
        this.xpath = xpath;
    }

    public final String getXPath(){
        return xpath;
    }
}
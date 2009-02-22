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

package jlibs.xml.sax.sniff.model.expr.num;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.expr.Function;
import org.jaxen.saxpath.Operator;

/**
 * @author Santhosh Kumar T
 */
public class Arithmetic extends Function{
    private int operator;

    public Arithmetic(Node contextNode, int operator){
        super(contextNode, Datatype.NUMBER, Datatype.NUMBER, Datatype.NUMBER);
        this.operator = operator;
    }

    @Override
    protected Object evaluate(Object[] args){
        double lhs = (Double)args[0];
        double rhs = (Double)args[1];
        switch(operator){
            case Operator.ADD:
                return lhs + rhs;
            case Operator.SUBTRACT:
                return lhs - rhs;
            case Operator.MULTIPLY:
                return lhs * rhs;
            case Operator.DIV:
                return lhs / rhs;
            case Operator.MOD:
                return lhs % rhs;
        }
        throw new ImpossibleException();
    }

    /*-------------------------------------------------[ ToString ]---------------------------------------------------*/

    @Override
    public String getName(){
        switch(operator){
            case Operator.ADD:
                return "+";
            case Operator.SUBTRACT:
                return "-";
            case Operator.MULTIPLY:
                return "*";
            case Operator.DIV:
                return "/";
            case Operator.MOD:
                return "%";
            default:
                throw new ImpossibleException(String.valueOf(operator));
        }
    }
}

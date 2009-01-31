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

package jlibs.xml.sax.sniff.model.computed.derived;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.model.ResultType;
import org.jaxen.saxpath.Operator;

/**
 * @author Santhosh Kumar T
 */
public class ArithmeticExpression extends DerivedResults{
    private int operator;

    public ArithmeticExpression(int operator){
        super(ResultType.NUMBER, false, ResultType.NUMBER, ResultType.NUMBER);
        this.operator = operator;
    }

    @Override
    protected String deriveResult(String[] memberResults){
        double lhs = Double.parseDouble(memberResults[0]);
        double rhs = Double.parseDouble(memberResults[1]);

        double result = 0;
        switch(operator){
            case Operator.ADD:
                result = lhs + rhs;
                break;
            case Operator.SUBTRACT:
                result = lhs - rhs;
                break;
            case Operator.MULTIPLY:
                result = lhs * rhs;
                break;
            case Operator.DIV:
                result = lhs / rhs;
                break;
            case Operator.MOD:
                result = lhs % rhs;
                break;
        }
        return String.valueOf(result);
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

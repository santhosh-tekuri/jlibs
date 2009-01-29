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

package jlibs.xml.sax.sniff.model.listeners;

import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.UserResults;
import org.jaxen.saxpath.Operator;

/**
 * @author Santhosh Kumar T
 */
public class ArithmeticOperation extends DerivedResults{
    private int operator;

    public ArithmeticOperation(int operator){
        this.operator = operator;
    }

    @Override
    public ResultType resultType(){
        return ResultType.NUMBER;
    }

    @Override
    public void prepareResults(){
        UserResults lhsMember = members.get(0);
        lhsMember.prepareResults();
        double lhs = lhsMember.asNumber();

        UserResults rhsMember = members.get(1);
        rhsMember.prepareResults();
        double rhs = rhsMember.asNumber();

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
        addResult(-1, String.valueOf(result));
    }
}

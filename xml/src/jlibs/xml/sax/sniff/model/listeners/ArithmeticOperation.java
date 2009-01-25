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

import org.jaxen.saxpath.Operator;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

/**
 * @author Santhosh Kumar T
 */
public class ArithmeticOperation extends DerivedResults{
    private int operator;

    public ArithmeticOperation(int operator){
        this.operator = operator;
    }

    @Override
    public QName resultType(){
        return XPathConstants.NUMBER;
    }

    public void joinResults(){
        double lhs = Double.parseDouble(getResult(members.get(0)));
        double rhs = Double.parseDouble(getResult(members.get(1)));
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

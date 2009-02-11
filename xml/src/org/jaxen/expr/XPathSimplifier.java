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

package org.jaxen.expr;

import jlibs.core.lang.ImpossibleException;
import org.jaxen.JaxenHandler;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;

import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XPathSimplifier{
    public XPathExpr simplify(XPathExpr xpathExpr){
        xpathExpr.setRootExpr(simplify(xpathExpr.getRootExpr()));
        return xpathExpr;
    }

    private Expr simplify(Expr expr){
        if(expr instanceof LocationPath)
            return simplify((LocationPath)expr);
        else if(expr instanceof DefaultUnaryExpr)
            return simplify((DefaultUnaryExpr)expr);
        else if(expr instanceof DefaultBinaryExpr)
            return simplify((DefaultBinaryExpr)expr);
        else if(expr instanceof FunctionCallExpr)
            return simplify((FunctionCallExpr)expr);
        else
            return expr;
    }

    @SuppressWarnings({"unchecked"})
    private Expr simplify(LocationPath expr){
        for(Step step: (List<Step>)expr.getSteps()){
            Iterator<Predicate> predicates = step.getPredicates().iterator();
            while(predicates.hasNext()){
                Predicate predicate = simplify(predicates.next());
                if(predicate==null)
                    predicates.remove();
            }
        }
        return expr;
    }

    private Predicate simplify(Predicate predicate){
        predicate.setExpr(simplify(predicate.getExpr()));
        return isFunction(predicate.getExpr(), "true") ? null : predicate;
    }

    private Expr simplify(DefaultUnaryExpr expr){
        if(expr.getExpr() instanceof NumberExpr){
            double d = ((NumberExpr)expr.getExpr()).getNumber().doubleValue();
            d*=-1;
            return new DefaultNumberExpr(d);
        }else
            return expr;
    }

    private Expr simplify(DefaultBinaryExpr expr){
        expr.setLHS(simplify(expr.getLHS()));
        expr.setRHS(simplify(expr.getRHS()));

        if(expr instanceof DefaultArithExpr)
            return simplify((DefaultArithExpr)expr);
        else if(expr instanceof DefaultRelationalExpr)
            return simplify((DefaultRelationalExpr)expr);
        else
            return expr;
    }

    private Expr simplify(DefaultArithExpr expr){
        if(expr.getLHS() instanceof NumberExpr && expr.getRHS() instanceof NumberExpr){
            double lhs = ((NumberExpr)expr.getLHS()).getNumber().doubleValue();
            double rhs = ((NumberExpr)expr.getRHS()).getNumber().doubleValue();
            double result;

            if(expr instanceof DefaultPlusExpr)
                result = lhs+rhs;
            else if(expr instanceof DefaultMinusExpr)
                result = lhs-rhs;
            else if(expr instanceof DefaultMultiplyExpr)
                result = lhs*rhs;
            else if(expr instanceof DefaultDivExpr)
                result = lhs/rhs;
            else if(expr instanceof DefaultModExpr)
                result = lhs%rhs;
            else
                throw new ImpossibleException(expr.getClass().getName());
            
            return new DefaultNumberExpr(result);
        }else
            return expr;
    }

    @SuppressWarnings({"deprecation"})
    private Expr simplify(DefaultRelationalExpr expr){
        if(expr.getLHS() instanceof NumberExpr && expr.getRHS() instanceof NumberExpr){
            double lhs = ((NumberExpr)expr.getLHS()).getNumber().doubleValue();
            double rhs = ((NumberExpr)expr.getRHS()).getNumber().doubleValue();
            boolean result;

            if(expr instanceof DefaultGreaterThanExpr)
                result = lhs>rhs;
            else if(expr instanceof DefaultGreaterThanEqualExpr)
                result = lhs>=rhs;
            else if(expr instanceof DefaultLessThanExpr)
                result = lhs<rhs;
            else if(expr instanceof DefaultLessThanEqualExpr)
                result = lhs<=rhs;
            else
                throw new ImpossibleException(expr.getClass().getName());

            return new DefaultFunctionCallExpr("", String.valueOf(result));
        }else
            return expr;
    }

    @SuppressWarnings({"unchecked"})
    private Expr simplify(FunctionCallExpr expr){
        for(int i=0; i<expr.getParameters().size(); i++)
            expr.getParameters().set(i, simplify((Expr)expr.getParameters().get(i)));
        return expr;
    }

    private boolean isFunction(Expr expr, String name){
        if(expr instanceof FunctionCallExpr){
            FunctionCallExpr function = (FunctionCallExpr)expr;
            if(function.getPrefix().equals("") && function.getFunctionName().equals(name))
                return true;
        }
        return false;
    }

    public static void main(String[] args) throws SAXPathException{
        XPathReader reader = XPathReaderFactory.createReader();
        JaxenHandler handler = new JaxenHandler();
        reader.setXPathHandler(handler);
        reader.parse("/a/b[5<6]");

        XPathExpr xpathExpr = handler.getXPathExpr();
        new XPathSimplifier().simplify(xpathExpr);
        System.out.println(xpathExpr);
    }
}

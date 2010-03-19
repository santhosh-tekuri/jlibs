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

package jlibs.xml.sax.dog.path;

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.func.FunctionCall;
import jlibs.xml.sax.dog.expr.func.Functions;
import jlibs.xml.sax.dog.expr.nodset.ExactPosition;
import jlibs.xml.sax.dog.expr.nodset.Positional;

/**
 * @author Santhosh Kumar T
 */
public class PredicateSet{
    protected Expression predicate;

    public boolean hasPosition;
    public PositionalPredicate headPositionalPredicate, tailPositionalPredicate;

    public int position, last;

    private int _position, _last;
    protected boolean hasPosition(Expression expr){
        if(expr instanceof Positional){
            Positional positional = (Positional)expr;
            positional.predicate = predicate;
            if(positional.position)
                _position++;
            else
                _last++;
            return true;
        }else if(expr instanceof FunctionCall){
            FunctionCall functionCall = (FunctionCall)expr;
            boolean hasPosition = false;
            for(Expression member: functionCall.members){
                if(hasPosition(member))
                    hasPosition = true;
            }
            return hasPosition;
        }
        return false;
    }

    public boolean impossible;
    public void setPredicate(Expression predicate){
        assert predicate.resultType== DataType.BOOLEAN : "predicate must of boolean type";

        if(impossible)
            return;

        // [exact-position(number)][exact-position(1)] can be simplified to [exact-position(number)]
        // [exact-position(number)][exact-position(numberNotEqualToOne)] can be simplified to <impossible>
        if(this.predicate instanceof ExactPosition && predicate instanceof ExactPosition){
            ExactPosition exactPosition = (ExactPosition)predicate;
            if(exactPosition.pos!=1)
                impossible = true;
            return;
        }

        if(predicate.scope()== Scope.GLOBAL){
            if(predicate.getResult()==Boolean.FALSE)
                impossible = true;
            return;
        }

        if(this.predicate!=null && this.predicate.scope()==Scope.DOCUMENT){
            FunctionCall and = new FunctionCall(Functions.AND);
            and.addValidMember(this.predicate, 0);
            and.addValidMember(predicate, 1);

            predicate = and;
            this.predicate = null;
        }

        _position = _last = 0;
        if(hasPosition(predicate)){
            hasPosition = true;
            if(this.predicate!=null){
                PositionalPredicate positionalPredicate = new PositionalPredicate(this.predicate, _position, _last);
                if(tailPositionalPredicate!=null){
                    tailPositionalPredicate.next = positionalPredicate;
                    tailPositionalPredicate = positionalPredicate;
                }else
                    headPositionalPredicate = tailPositionalPredicate = positionalPredicate;
                this.predicate = null;
            }else{
                position = _position;
                last = _last;
            }
        }

        if(this.predicate==null)
            this.predicate = predicate;
        else{
            FunctionCall and = new FunctionCall(Functions.AND);
            and.addValidMember(this.predicate, 0);
            and.addValidMember(predicate, 1);
            this.predicate = and;
        }
    }

    public Expression getPredicate(){
        return predicate;
    }
}

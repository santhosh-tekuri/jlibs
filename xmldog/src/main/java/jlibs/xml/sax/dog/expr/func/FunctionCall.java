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

package jlibs.xml.sax.dog.expr.func;

import jlibs.xml.ClarkName;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.expr.nodset.ExactPosition;
import jlibs.xml.sax.dog.expr.nodset.Position;
import jlibs.xml.sax.dog.expr.nodset.Strings;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class FunctionCall extends Expression{
    public final Function function;

    public FunctionCall(Function function){
        super(Scope.GLOBAL, function.resultType);
        this.function = function;
        members = new Expression[function.mandatory];
    }

    public FunctionCall(Function function, int noOfMembers){
        super(Scope.GLOBAL, function.resultType);
        this.function = function;
        if(!function.canAccept(noOfMembers))
            throw new IllegalArgumentException(String.format("%s function cannot accept %d arguments", function.name, noOfMembers));
        members = new Expression[noOfMembers];
    }

    private FunctionCall(Function function, Expression members[], int scope){
        super(scope, function.resultType);
        this.function = function;
        this.members = members;
    }

    public final Expression members[];

    public Expression addMember(Object member, int i){
        assert members[i]==null : "overwriting exising member";
        Expression expr = Functions.typeCast(member, function.memberType(i));
        int memberScope = expr.scope();
        if(scope<memberScope)
            scope = memberScope;
        return members[i]=expr;
    }

    public Expression addValidMember(Expression member, int i){
        assert members[i]==null : "overwriting exising member";
        assert member == Functions.typeCast(member, function.memberType(i));
        int memberScope = member.scope();
        if(scope<memberScope)
            scope = memberScope;
        return members[i]=member;
    }

    @Override
    public Object getResult(){
        Object memberResults[] = new Object[members.length];

        int i=0;
        for(Expression member: members){
            memberResults[i] = member.getResult();
            i++;
        }
        return function.evaluate(memberResults);
    }

    @Override
    public Object getResult(Event event){
        Function function = this.function;
        Expression members[] = this.members;

        PeekingFunction peekingFunction = function instanceof PeekingFunction ? (PeekingFunction)function: null;

        int pending = members.length;
        Object memberResults[] = new Object[pending];

        for(int i=0, len=pending; i<len; i++){
            Object memberResult = event.evaluate(members[i]);
            if(memberResult!=null){
                if(--pending>0 && peekingFunction!=null){
                    Object result = peekingFunction.onMemberResult(i, memberResult);
                    if(result!=null)
                        return result;
                }
                memberResults[i] = memberResult;
            }else
                memberResults[i] = event.evaluation;
        }

        if(pending==0)
            return function.evaluate(memberResults);
        else{
            if(peekingFunction!=null)
                return new PeekingFunctionEvaluation(this, event, memberResults, pending);
            else
                return new FunctionEvaluation(this, event, memberResults, pending);
        }
    }

    @Override
    public Expression simplify(){
        if(scope==Scope.GLOBAL)
            return super.simplify();

        if(function==Functions.EQUALS){
            if(scope==Scope.LOCAL){
                Double d = null;
                if(members[0] instanceof Position && members[1].scope()==Scope.GLOBAL) // position()=number can be simplified to exact-position(number)
                    d = (Double)members[1].getResult();
                else if(members[1] instanceof Position && members[0].scope()==Scope.GLOBAL) // number=position() can be simplified to exact-position(number)
                    d = (Double)members[0].getResult();

                if(d!=null){
                    int pos = d.intValue();
                    if(d!=pos)
                        return new Literal(Boolean.FALSE, DataType.BOOLEAN);
                    else
                        return new ExactPosition(pos);
                }
            }

            DataType member0Type = members[0].resultType;
            DataType member1Type = members[1].resultType;
            if(member0Type==DataType.NUMBER){
                if(member1Type==DataType.NUMBER)
                    return new FunctionCall(Functions.NUMBER_EQUALS_NUMBER, members, scope);
                else if(member1Type==DataType.STRINGS){
                    FunctionCall functionCall = new FunctionCall(Functions.NUMBERS_EQUALS_NUMBER);
                    Strings member1 = (Strings)members[1];
                    functionCall.members[0] = new Strings(member1.locationPath, DataType.NUMBERS, true, false);
                    functionCall.members[1] = members[0];
                    functionCall.scope = scope;
                    return functionCall;
                }else
                    System.out.println(members[0].resultType+"=="+members[1].resultType);
            }else if(member0Type==DataType.STRING){
                if(member1Type==DataType.STRING)
                    return new FunctionCall(Functions.STRING_EQUALS_STRING, members, scope);
                else if(member1Type==DataType.STRINGS){
                    FunctionCall functionCall = new FunctionCall(Functions.STRINGS_EQUALS_STRING);
                    functionCall.members[0] = members[1];
                    functionCall.members[1] = members[0];
                    functionCall.scope = scope;
                    return functionCall;
                }else
                    System.out.println(members[0].resultType+"=="+members[1].resultType);
            }else if(member0Type==DataType.STRINGS){
                if(member1Type==DataType.STRINGS)
                    return new FunctionCall(Functions.STRINGS_EQUALS_STRINGS, members, scope);
                else if(member1Type==DataType.STRING)
                    return new FunctionCall(Functions.STRINGS_EQUALS_STRING, members, scope);
                else if(member1Type==DataType.NUMBER){
                    Strings member0 = (Strings)members[0];
                    members[0] = new Strings(member0.locationPath, DataType.NUMBERS, true, false);
                    return new FunctionCall(Functions.NUMBERS_EQUALS_NUMBER, members, scope);
                }else
                    System.out.println(members[0].resultType+"=="+members[1].resultType);
            }else
                System.out.println(members[0].resultType+"=="+members[1].resultType);
        }
        return this;
    }

    @Override
    public String toString(){
        String separator = ", ";
        boolean operator = function.isOperator();
        if(operator)
            separator = " "+function.name+" ";

        StringBuilder buff = new StringBuilder();
        for(Expression member: members){
            if(buff.length()>0)
                buff.append(separator);
            boolean enclose = operator && member instanceof FunctionCall && ((FunctionCall)member).function.isOperator();
            if(enclose)
                buff.append('(');
            buff.append(member);
            if(enclose)
                buff.append(')');
        }
        if(separator.equals(", "))
            return String.format("%s(%s)", ClarkName.valueOf(function.namespace, function.name), buff);
        else
            return buff.toString();
    }
}

class FunctionEvaluation extends Evaluation<FunctionCall>{
    protected int pending;
    protected final Object[] memberResults;
    protected final Event event;

    public FunctionEvaluation(FunctionCall expression, Event event, Object memberResults[], int pending){
        super(expression, event.order());
        this.event = event;
        this.memberResults = memberResults;
        this.pending = pending;
    }

    @Override
    public final void start(){
        Object[] memberResults = this.memberResults;
        for(int i=0, len=memberResults.length; i<len; i++){
            Object memberResult = memberResults[i];
            if(memberResult instanceof Evaluation){
                Evaluation eval = (Evaluation)memberResult;
                eval.addListener(this);
                eval.start();
            }else if(memberResult==null){
                assert expression.members[i].scope()==Scope.DOCUMENT;
                event.addListener(expression.members[i], this);
            }
            if(result!=null)
                return;
        }
        if(result==null && pending==0)
            fireFinished();
    }

    protected void setMemberResult(int i, Object memberResult){
        memberResults[i] = memberResult;
        pending--;
    }

    protected Object result;

    @Override
    public final Object getResult(){
        return result;
    }

    @Override
    public final void finished(Evaluation evaluation){
        assert result==null : "can't consume any child evaluation";

        Expression item = evaluation.expression;
        Expression members[] = expression.members;
        for(int i=members.length-1; i>=0; --i){
            if(members[i]==item){
                setMemberResult(i, evaluation.getResult());
                if(pending==0)
                    fireFinished();
                return;
            }
        }
        assert false: "Impossible";
    }

    @Override
    protected final void fireFinished(){
        if(result==null)
            result = expression.function.evaluate(memberResults);
        super.fireFinished();
    }

    @Override
    protected final void dispose(){
        assert pending!=0;

        for(int i=0, len=memberResults.length; i<len; i++){
            Object memberResult = memberResults[i];
            if(memberResult instanceof Evaluation)
                ((Evaluation)memberResult).removeListener(this);
            else if(memberResult==null){
                assert expression.members[i].scope()==Scope.DOCUMENT;
                event.removeListener(expression.members[i], this);
            }
        }
    }
}

class PeekingFunctionEvaluation extends FunctionEvaluation{
    private final PeekingFunction function;

    public PeekingFunctionEvaluation(FunctionCall expression, Event event, Object memberResults[], int pending){
        super(expression, event, memberResults, pending);
        function = (PeekingFunction)expression.function;
    }

    @Override
    protected void setMemberResult(int memberIndex, Object memberResult){
        super.setMemberResult(memberIndex, memberResult);
        if(pending>0){
            if((result=function.onMemberResult(memberIndex, memberResult))!=null){
                fireFinished();
                dispose();
            }
        }
    }
}

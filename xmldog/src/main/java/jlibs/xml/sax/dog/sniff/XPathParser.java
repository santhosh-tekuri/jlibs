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

package jlibs.xml.sax.dog.sniff;

import jlibs.core.lang.NotImplementedException;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.expr.func.Function;
import jlibs.xml.sax.dog.expr.func.FunctionCall;
import jlibs.xml.sax.dog.expr.func.Functions;
import jlibs.xml.sax.dog.expr.nodset.ExactPosition;
import jlibs.xml.sax.dog.expr.nodset.Language;
import jlibs.xml.sax.dog.expr.nodset.Last;
import jlibs.xml.sax.dog.expr.nodset.Position;
import jlibs.xml.sax.dog.path.*;
import jlibs.xml.sax.dog.path.tests.*;
import org.jaxen.saxpath.Operator;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathHandler;
import org.jaxen.saxpath.XPathReader;

import javax.xml.namespace.NamespaceContext;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public final class XPathParser implements XPathHandler{
    private final NamespaceContext nsContext;
    private final XPathReader reader = new org.jaxen.saxpath.base.XPathReader();

    public XPathParser(NamespaceContext nsContext){
        this.nsContext = nsContext;
        reader.setXPathHandler(this);
    }

    public Expression parse(String xpath) throws SAXPathException{
        stack.clear();
        peekFrame = null;
        stepStack.clear();
        noOfLocationPaths = 0;
        expr = null;

        reader.parse(xpath);
        return expr;
    }

    /*-------------------------------------------------[ XPath ]---------------------------------------------------*/

    @Override
    public void startXPath(){
        pushFrame();
    }

    private Expression expr;

    @Override
    public void endXPath(){
        Object current = pop();
        if(current instanceof Expression)
            expr = (Expression)current;
        else
            expr = ((LocationPath)current).typeCast(DataType.NODESET).simplify();
    }

    /*-------------------------------------------------[ LocationPath ]---------------------------------------------------*/

    private int noOfLocationPaths;

    @Override
    public void startAbsoluteLocationPath(){
        noOfLocationPaths++;
        pushFrame();
    }

    @Override
    public void endAbsoluteLocationPath(){
        endLocationPath(Scope.DOCUMENT);
    }

    @Override
    public void startRelativeLocationPath(){
        noOfLocationPaths++;
        pushFrame();
    }

    @Override
    public void endRelativeLocationPath(){
        endLocationPath(noOfLocationPaths==1 ? Scope.DOCUMENT : Scope.LOCAL);
    }

    @SuppressWarnings({"unchecked"})
    private void endLocationPath(int scope){
        noOfLocationPaths--;
        ArrayDeque stack = popFrame();
        LocationPath path = new LocationPath(scope, stack.size());
        stack.toArray(path.steps);
        push(LocationPathAnalyzer.simplify(path));
    }

    /*-------------------------------------------------[ Steps ]---------------------------------------------------*/

    private static final int axisMap[] = {
        -1, //INVALID_AXIS
        Axis.CHILD,
        Axis.DESCENDANT,
        -1, //PARENT
        -1, //ANCESTOR
        Axis.FOLLOWING_SIBLING,
        -1, //PRECEDING_SIBLING
        Axis.FOLLOWING,
        -1, //PRECEDING
        Axis.ATTRIBUTE,
        Axis.NAMESPACE,
        Axis.SELF,
        Axis.DESCENDANT_OR_SELF,
        -1, //ANCESTOR_OR_SELF
    };

    private ArrayDeque<Step> stepStack = new ArrayDeque<Step>();

    private void startStep(int axis, Constraint constraint){
        int dogAxis = axisMap[axis];
        if(dogAxis==-1)
            throw new UnsupportedOperationException("Axis "+org.jaxen.saxpath.Axis.lookup(axis)+" is not supported");
        Step step = new Step(dogAxis, constraint);
        push(step);
        stepStack.addLast(step);
    }

    private void endStep(){
        stepStack.pollLast();
    }

    @Override
    public void startNameStep(int axis, String prefix, String localName) throws SAXPathException{
        Constraint constraint;

        boolean star = localName.equals("*");
        if(star && prefix.length()==0)
            constraint = Star.INSTANCE;
        else{
            String uri = nsContext.getNamespaceURI(prefix);
            if(uri==null)
                throw new SAXPathException("undeclared prefix: " + prefix);
            constraint = star ? namespaceURIStub.get(uri) : qnameStub.get(uri, localName);
        }
        startStep(axis, constraint);
    }

    @Override
    public void endNameStep(){
        endStep();
    }

    @Override
    public void startAllNodeStep(int axis){
        startStep(axis, Node.INSTANCE);
    }

    @Override
    public void endAllNodeStep(){
        endStep();
    }

    @Override
    public void startTextNodeStep(int axis){
        startStep(axis, Text.INSTANCE);
    }

    @Override
    public void endTextNodeStep(){
        endStep();
    }

    @Override
    public void startCommentNodeStep(int axis){
        startStep(axis, Comment.INSTANCE);
    }

    @Override
    public void endCommentNodeStep(){
        endStep();
    }

    @Override
    public void startProcessingInstructionNodeStep(int axis, String name){
        startStep(axis, name.length()==0 ? PI.INSTANCE : piTargetStub.get(name));
    }

    @Override
    public void endProcessingInstructionNodeStep(){
        endStep();
    }

    /*-------------------------------------------------[ Predicate ]---------------------------------------------------*/

    @Override
    public void startPredicate(){}

    @Override
    public void endPredicate(){
        Object predicate = pop();
        Step step = (Step)peek();

        Expression predicateExpr;
        if(predicate instanceof Expression){
            predicateExpr = (Expression)predicate;
            if(predicateExpr.resultType==DataType.NUMBER){
                if(predicate instanceof Literal){
                    Double d = (Double)predicateExpr.getResult();
                    int pos = d.intValue();
                    if(d!=pos)
                        predicateExpr = new Literal(Boolean.FALSE, DataType.BOOLEAN);
                    else{
                        if(step.axis==Axis.SELF
                                || ((step.axis==Axis.ATTRIBUTE || step.axis==Axis.NAMESPACE) && step.constraint instanceof QName)
                                || step.getPredicate() instanceof ExactPosition)
                            predicateExpr = new Literal(pos==1, DataType.BOOLEAN);
                        else
                            predicateExpr = new ExactPosition(pos);
                    }
                }else{
                    FunctionCall equals = new FunctionCall(Functions.NUMBER_EQUALS_NUMBER);
                    equals.addValidMember(new Position(), 0);
                    equals.addValidMember(predicateExpr, 1);
                    predicateExpr = equals;
                }
            }else
                predicateExpr = Functions.typeCast(predicateExpr, DataType.BOOLEAN);
        }else
            predicateExpr = Functions.typeCast(predicate, DataType.BOOLEAN);
        step.setPredicate(predicateExpr);
    }

    /*-------------------------------------------------[ Literals ]---------------------------------------------------*/

    @Override
    public void literal(String literal){
        push(new Literal(literal, DataType.STRING));
    }

    @Override
    @SuppressWarnings({"UnnecessaryBoxing"})
    public void number(int number){
        push(new Literal(new Double(number), DataType.NUMBER));
    }

    @Override
    @SuppressWarnings({"UnnecessaryBoxing"})
    public void number(double number){
        push(new Literal(new Double(number), DataType.NUMBER));
    }

    /*-------------------------------------------------[ Operators ]---------------------------------------------------*/

    @Override
    public void startUnaryExpr(){}

    @Override
    public void endUnaryExpr(int operator){
        FunctionCall functionCall = new FunctionCall(Functions.MULTIPLY);
        functionCall.addValidMember(new Literal(-1d, DataType.NUMBER), 0);
        functionCall.addMember(pop(), 1);
        push(functionCall.simplify());
    }

    @Override
    public void startEqualityExpr(){}

    @Override
    public void endEqualityExpr(int operator){
        endBinaryOperator(operator==Operator.EQUALS ? Functions.EQUALS : Functions.NOT_EQUALS);
    }

    @Override
    public void startAdditiveExpr(){}

    @Override
    public void endAdditiveExpr(int operator){
        endBinaryOperator(operator==Operator.ADD ? Functions.ADD : Functions.SUBSTRACT);
    }

    @Override
    public void startMultiplicativeExpr(){}

    @Override
    public void endMultiplicativeExpr(int operator){
        Function function;
        if(operator==Operator.MULTIPLY)
            function = Functions.MULTIPLY;
        else if(operator==Operator.DIV)
            function = Functions.DIV;
        else
            function = Functions.MOD;

        endBinaryOperator(function);
    }

    @Override
    public void startRelationalExpr(){}

    @Override
    public void endRelationalExpr(int operator){
        Function function;
        if(operator==Operator.LESS_THAN)
            function = Functions.LESS_THAN;
        else if(operator==Operator.LESS_THAN_EQUALS)
            function = Functions.LESS_THAN_EQUAL;
        else if(operator==Operator.GREATER_THAN)
            function = Functions.GREATER_THAN;
        else
            function = Functions.GREATER_THAN_EQUAL;

        endBinaryOperator(function);
    }

    @Override
    public void startAndExpr(){}

    @Override
    public void endAndExpr(boolean create){
        if(create)
            endBinaryOperator(Functions.AND);
    }

    @Override
    public void startOrExpr(){}

    @Override
    public void endOrExpr(boolean create){
        if(create)
            endBinaryOperator(Functions.OR);
    }

    private void endBinaryOperator(Function function){
        FunctionCall functionCall = new FunctionCall(function);
        Object member2 = pop();
        Object member1 = pop();
        functionCall.addMember(member1, 0);
        functionCall.addMember(member2, 1);
        push(functionCall.simplify());
    }

    @Override
    public void startFunction(String prefix, String name) throws SAXPathException{
        String uri = nsContext.getNamespaceURI(prefix);
        if(uri==null)
            throw new SAXPathException("undeclared prefix: " + prefix);

        if(uri.length()!=0)
            throw new NotImplementedException();
        pushFrame();
        push(name);
    }

    @Override
    public void endFunction() throws SAXPathException{
        ArrayDeque stack = popFrame();
        String name = (String)stack.pollFirst();
        push(createFunction(name, stack).simplify());
    }

    public boolean langInterested;
    private Expression createFunction(String name, ArrayDeque params) throws SAXPathException{
        int noOfParams = params.size();
        switch(noOfParams){
            case 0:{
                LocationPath locationPath = noOfLocationPaths==0 ? LocationPath.DOCUMENT_CONTEXT : LocationPath.LOCAL_CONTEXT;
                Expression expr = locationPath.apply(name);
                if(expr!=null)
                    return expr;
                return createFunction(name, 0);
            }
            case 1:{
                if(name.equals("lang")){
                    langInterested = true;
                    FunctionCall functionCall = new FunctionCall(Functions.LANGUAGE_MATCH);
                    functionCall.addValidMember(new Language(), 0);
                    functionCall.addMember(params.pollFirst(), 1);
                    return functionCall;
                } else{
                    Object current = params.pollFirst();
                    if(current instanceof LocationPath){
                        Expression expr = ((LocationPath)current).apply(name);
                        if(expr!=null)
                            return expr;
                    }
                    FunctionCall functionCall = (FunctionCall)createFunction(name, 1);
                    functionCall.addMember(current, 0);
                    return functionCall;
                }
            }
            default:
                FunctionCall functionCall = (FunctionCall)createFunction(name, noOfParams);
                for(int i = 0; i<noOfParams; i++)
                    functionCall.addMember(params.pollFirst(), i);
                return functionCall;
        }
    }

    private Expression createFunction(String name, int noOfMembers) throws SAXPathException{
        if(noOfMembers==0){
            if(name.equals("position")){
                Step step = stepStack.peekLast();
                int axis = step.axis;
                if(axis==Axis.SELF
                        || ((axis==Axis.ATTRIBUTE || axis==Axis.NAMESPACE) && step.constraint instanceof QName)
                        || step.getPredicate() instanceof ExactPosition)
                    return new Literal(DataType.ONE, DataType.NUMBER);
                else
                    return new Position();
            } else if(name.equals("last")){
                Step step = stepStack.peekLast();
                int axis = step.axis;
                if(axis==Axis.SELF
                        || ((axis==Axis.ATTRIBUTE || axis==Axis.NAMESPACE) && step.constraint instanceof QName)
                        || step.getPredicate() instanceof ExactPosition)
                    return new Literal(DataType.ONE, DataType.NUMBER);
                else
                    return new Last();
            } else if(name.equals("true"))
                return new Literal(Boolean.TRUE, DataType.BOOLEAN);
            else if(name.equals("false"))
                return new Literal(Boolean.FALSE, DataType.BOOLEAN);
        }

        Function function = Functions.library.get(name);
        if(function==null)
            throw new SAXPathException("Unknown function: " + name);
        return new FunctionCall(function, noOfMembers);
    }

    /*-------------------------------------------------[ Pending ]---------------------------------------------------*/

    @Override
    public void startUnionExpr(){}

    @Override
    public void endUnionExpr(boolean create){
        if(create)
            throw new NotImplementedException("Union");
    }

    private static final Object FILTER_FLAG = new Object();

    @Override
    public void startFilterExpr(){
        push(FILTER_FLAG);
    }

    @Override
    public void endFilterExpr(){
        Object obj = pop();
        if(pop()!=FILTER_FLAG)
            throw new NotImplementedException("FilterExpression");
        push(obj);
    }

    private static final Object PATH_FLAG = new Object();

    @Override
    public void startPathExpr(){
        push(PATH_FLAG);
    }

    @Override
    public void endPathExpr(){
        Object obj = pop();
        if(pop()!=PATH_FLAG)
            throw new NotImplementedException("Path");
        push(obj);
    }

    @Override
    public void variableReference(String prefix, String variableName){
        throw new NotImplementedException("VariableReference");
    }

    /*-------------------------------------------------[ Context ]---------------------------------------------------*/

    private ArrayDeque<ArrayDeque> stack = new ArrayDeque<ArrayDeque>();
    private ArrayDeque peekFrame;

    private void pushFrame(){
        stack.addLast(peekFrame=new ArrayDeque());
    }

    private ArrayDeque popFrame(){
        ArrayDeque frame = stack.pollLast();
        peekFrame = stack.peekLast();
        return frame;
    }

    @SuppressWarnings({"unchecked"})
    private void push(Object obj){
        peekFrame.addLast(obj);
    }

    private Object pop(){
        return peekFrame.pollLast();
    }

    private Object peek(){
        return peekFrame.peekLast();
    }

    /*-------------------------------------------------[ Stubs ]---------------------------------------------------*/

    public ArrayList constraints = new ArrayList();
    private NamespaceURIStub namespaceURIStub = new NamespaceURIStub();
    private QNameStub qnameStub = new QNameStub();
    private PITargetStub piTargetStub = new PITargetStub();

    class NamespaceURIStub{
        private String namespaceURI;

        @Override
        public boolean equals(Object obj){
            return obj instanceof NamespaceURI && ((NamespaceURI)obj).namespaceURI.equals(namespaceURI);
        }

        @SuppressWarnings({"unchecked"})
        public NamespaceURI get(String namespaceURI){
            this.namespaceURI = namespaceURI;
            int index = constraints.indexOf(this);
            if(index!=-1)
                return (NamespaceURI)constraints.get(index);
            else{
                NamespaceURI constraint = new NamespaceURI(Constraint.ID_START +constraints.size(), namespaceURI);
                constraints.add(constraint);
                return constraint;
            }
        }
    }

    class QNameStub{
        private String namespaceURI;
        private String localName;

        @Override
        public boolean equals(Object obj){
            if(obj instanceof QName){
                QName qname = (QName)obj;
                return qname.localName.equals(localName) && qname.namespaceURI.equals(namespaceURI);
            }else
                return false;
        }

        @SuppressWarnings({"unchecked"})
        public QName get(String namespaceURI, String localName){
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            int index = constraints.indexOf(this);
            if(index!=-1)
                return (QName)constraints.get(index);
            else{
                QName constraint = new QName(Constraint.ID_START +constraints.size(), namespaceURI, localName);
                constraints.add(constraint);
                return constraint;
            }
        }
    }

    class PITargetStub{
        private String target;

        @Override
        public boolean equals(Object obj){
            return obj instanceof PITarget && ((PITarget)obj).target.equals(target);
        }

        @SuppressWarnings({"unchecked"})
        public PITarget get(String target){
            this.target = target;
            int index = constraints.indexOf(this);
            if(index!=-1)
                return (PITarget)constraints.get(index);
            else{
                PITarget constraint = new PITarget(Constraint.ID_START +constraints.size(), target);
                constraints.add(constraint);
                return constraint;
            }
        }
    }
}

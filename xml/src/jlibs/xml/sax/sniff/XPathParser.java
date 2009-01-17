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

package jlibs.xml.sax.sniff;

import jlibs.core.lang.StringUtil;
import jlibs.xml.sax.sniff.model.*;
import jlibs.xml.sax.sniff.model.functions.Function;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathHandler;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;

/**
 * @author Santhosh Kumar T
 */
public class XPathParser implements XPathHandler{
    private Root root;

    public XPathParser(Root root){
        this.root = root;
    }

    private XPathReader reader;
    private Node current;
    public XPath parse(String xpath) throws SAXPathException{
        if(reader==null){
            reader = XPathReaderFactory.createReader();
            reader.setXPathHandler(this);
        }
        current = root;
        reader.parse(xpath);

        return predicates.size()>0 ? new XPath(xpath, predicates, true) : new XPath(xpath, Collections.singletonList(current));
    }

    @Override
    public void startXPath() throws SAXPathException{}

    @Override
    public void endXPath() throws SAXPathException{
        if(predicates.size()>0){
            ArrayList<Predicate> newPredicates = new ArrayList<Predicate>();
//            for(int i=0; i<currents.size(); i++){
//                Node current = currents.get(i);
                Predicate predicate = predicates.get(0);
                if(!current.memberOf.contains(predicate)){
                    Predicate newPredicate = new Predicate(predicate);
                    newPredicate.userGiven = true;
                    current.addPredicate(newPredicate);
                    newPredicates.add(newPredicate);
                }else
                    predicate.userGiven = true;
//            }
            
            if(newPredicates.size()>0)
                predicates = newPredicates;
        }else{
            current.userGiven = true;
        }
    }

    @Override
    public void startPathExpr() throws SAXPathException{}

    @Override
    public void endPathExpr() throws SAXPathException{}

    @Override
    public void startAbsoluteLocationPath() throws SAXPathException{
        if(!pathStack.isEmpty())
            pathStack.push(pathStack.pop()+1);
        current = root;
    }

    @Override
    public void endAbsoluteLocationPath() throws SAXPathException{}

    @Override
    public void startRelativeLocationPath() throws SAXPathException{
        if(!pathStack.isEmpty())
            pathStack.push(pathStack.pop()+1);
    }

    @Override
    public void endRelativeLocationPath() throws SAXPathException{}

    @Override
    public void startNameStep(int axis, String prefix, String localName) throws SAXPathException{
        startAllNodeStep(axis);
        
        if(localName.equals("*"))
            localName = null;
        String uri = root.nsContext.getNamespaceURI(prefix);
        if(uri==null)
            throw new SAXPathException("undeclared prefix: "+prefix);
        if(StringUtil.isEmpty(uri) && localName==null)
            uri = null;

        current = current.addConstraint(new QNameNode(uri, localName));
    }

    @Override
    public void endNameStep() throws SAXPathException{}

    @Override
    public void startTextNodeStep(int axis) throws SAXPathException{
        startAllNodeStep(axis);
        current = current.addConstraint(new Text());
    }

    @Override
    public void endTextNodeStep() throws SAXPathException{}

    @Override
    public void startCommentNodeStep(int axis) throws SAXPathException{
        startAllNodeStep(axis);
        current = current.addConstraint(new Comment());
    }

    @Override
    public void endCommentNodeStep() throws SAXPathException{}

    private int currentAxis;

    @Override
    public void startAllNodeStep(int axis) throws SAXPathException{
        if(axis==Axis.SELF)
            return; //do nothing
        
        AxisNode axisNode = AxisNode.newInstance(axis);
        boolean self = axis==Axis.DESCENDANT_OR_SELF || axis==Axis.ANCESTOR_OR_SELF;
        if(self)
            current = current.addConstraint(axisNode);
        else
            current = current.addChild(axisNode);

        currentAxis = axis;
    }

    @Override
    public void endAllNodeStep() throws SAXPathException{}

    @Override
    public void startProcessingInstructionNodeStep(int axis, String name) throws SAXPathException{
        if(StringUtil.isEmpty(name)) // saxpath gives name="" for processing-instruction() i.e without argument
            name = null;

        startAllNodeStep(axis);
        current = current.addConstraint(new ProcessingInstruction(name));
    }

    @Override
    public void endProcessingInstructionNodeStep() throws SAXPathException{}

    private Deque<Node> predicateContext = new ArrayDeque<Node>();
    private ArrayDeque<Integer> pathStack = new ArrayDeque<Integer>();

    @Override
    public void startPredicate() throws SAXPathException{
        predicates.clear();
        predicateContext.push(current);
        pathStack.push(0);
    }

    private ArrayList<Predicate> predicates = new ArrayList<Predicate>();
    @Override
    public void endPredicate() throws SAXPathException{
        if(pathStack.pop()>0){
            Node context = predicateContext.pop();
            Predicate predicate = new Predicate(current);
            predicates.add(predicate);
            context.addPredicate(predicate);
            current = context;
        }else
            predicateContext.pop();
    }

    @Override
    public void startFilterExpr() throws SAXPathException{}

    @Override
    public void endFilterExpr() throws SAXPathException{}

    @Override
    public void startOrExpr() throws SAXPathException{}

    @Override
    public void endOrExpr(boolean create) throws SAXPathException{
        if(create)
            throw new SAXPathException("Or expression is unsupprted");
    }

    @Override
    public void startAndExpr() throws SAXPathException{}

    @Override
    public void endAndExpr(boolean create) throws SAXPathException{
        if(create)
            throw new SAXPathException("And expression is unsupprted");
    }

    @Override
    public void startEqualityExpr() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void endEqualityExpr(int equalityOperator) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void startRelationalExpr() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void endRelationalExpr(int relationalOperator) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void startAdditiveExpr() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void endAdditiveExpr(int additiveOperator) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void startMultiplicativeExpr() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void endMultiplicativeExpr(int multiplicativeOperator) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void startUnaryExpr() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void endUnaryExpr(int unaryOperator) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void startUnionExpr() throws SAXPathException{}

    @Override
    public void endUnionExpr(boolean create) throws SAXPathException{
        if(create)
            throw new SAXPathException("Union expression is unsupprted");
    }

    @Override
    public void number(int number) throws SAXPathException{
        if(predicateContext.size()>0)
            current = current.addConstraint(new Position(currentAxis, number));
        else
            throw new SAXPathException("unsupprted");
    }

    @Override
    public void number(double number) throws SAXPathException{
        number((int)number);
    }

    @Override
    public void literal(String literal) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void variableReference(String prefix, String variableName) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    private Function function;
    @Override
    public void startFunction(String prefix, String functionName) throws SAXPathException{
        if(prefix.length()>0)
            throw new SAXPathException("unsupprted function "+prefix+':'+functionName+"()");

        function = Function.newInstance(functionName);
    }

    @Override
    public void endFunction() throws SAXPathException{
        current = current.addConstraint(function);
        predicates.clear();
    }
}

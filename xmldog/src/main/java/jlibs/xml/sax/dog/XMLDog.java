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

package jlibs.xml.sax.dog;

import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.expr.func.FunctionCall;
import jlibs.xml.sax.dog.expr.nodset.LocationExpression;
import jlibs.xml.sax.dog.expr.nodset.PathExpression;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.path.PositionalPredicate;
import jlibs.xml.sax.dog.path.Step;
import jlibs.xml.sax.dog.sniff.Event;
import jlibs.xml.sax.dog.sniff.SAXEngine;
import jlibs.xml.sax.dog.sniff.STAXEngine;
import jlibs.xml.sax.dog.sniff.XPathParser;
import org.jaxen.saxpath.SAXPathException;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class XMLDog{
    public final NamespaceContext nsContext;
    public final XPathVariableResolver variableResolver;
    public final XPathFunctionResolver functionResolver;
    private final XPathParser parser;

    public XMLDog(NamespaceContext nsContext, XPathVariableResolver variableResolver, XPathFunctionResolver functionResolver){
        this.nsContext = nsContext;
        this.variableResolver = variableResolver;
        this.functionResolver = functionResolver;
        parser = new XPathParser(nsContext, variableResolver, functionResolver);
    }

    private final List<Expression> expressions = new ArrayList<Expression>();
    private final List<Expression> docExpressions = new ArrayList<Expression>();
    private final ArrayDeque<Expression> tempStack = new ArrayDeque<Expression>();

    public Expression addXPath(String xpath) throws SAXPathException{
        Expression compiledExpr = parser.parse(xpath);
        compiledExpr.setXPath(xpath);
        expressions.add(compiledExpr);

        assert compiledExpr.scope()!=Scope.LOCAL;
        assert compiledExpr.scope()!=Scope.GLOBAL || compiledExpr instanceof Literal;
        if(compiledExpr.scope()==Scope.DOCUMENT){
            List<Expression> docExpressions = this.docExpressions;
            int id = docExpressions.size();
            ArrayDeque<Expression> tempStack = this.tempStack;
            tempStack.addLast(compiledExpr);
            while(!tempStack.isEmpty()){
                Expression expr = tempStack.pollLast();

                if(expr.scope()==Scope.DOCUMENT){
                    expr.id = id++;
                    docExpressions.add(expr);
                }

                if(expr instanceof LocationExpression){
                    for(Step step: ((LocationExpression)expr).locationPath.steps){
                        Expression predicate = step.predicateSet.getPredicate();
                        if(predicate!=null){
                            if(predicate.scope()!=Scope.GLOBAL)
                                tempStack.addLast(predicate);  
                            for(PositionalPredicate positionPredicate=step.predicateSet.headPositionalPredicate; positionPredicate!=null; positionPredicate=positionPredicate.next){
                                if(positionPredicate.predicate.scope()!=Scope.GLOBAL)
                                    tempStack.addLast(positionPredicate.predicate);
                            }
                        }
                    }
                }else if(expr instanceof FunctionCall){
                    FunctionCall functionCall = (FunctionCall)expr;
                    for(Expression member: functionCall.members){
                        if(member.scope()!=Scope.GLOBAL)
                            tempStack.add(member);
                    }
                }else if(expr instanceof PathExpression){
                    PathExpression pathExpr = (PathExpression)expr;
                    if(pathExpr.union.predicateSet.getPredicate()!=null)
                        tempStack.add(pathExpr.union.predicateSet.getPredicate());
                    for(LocationExpression context: pathExpr.contexts)
                        tempStack.add(context);
                    tempStack.add(pathExpr.relativeExpression);
                }
            }
        }

        return compiledExpr;
    }

    public Iterable<Expression> getXPaths(){
        return expressions;
    }

    public int getDocumentXPathsCount(){
        return docExpressions.size();
    }

    @SuppressWarnings({"unchecked"})
    public Event createEvent(){
        return new Event(nsContext, docExpressions, Constraint.ID_START+parser.constraints.size());
    }

    /*-------------------------------------------------[ Sniff ]---------------------------------------------------*/

    public void sniff(Event event, String uri, boolean useSTAX) throws XPathException{
        if(docExpressions.size()>0){
            if(useSTAX)
                new STAXEngine(event, parser.langInterested).start(uri);
            else
                new SAXEngine(event, parser.langInterested).start(uri);
        }
    }

    public XPathResults sniff(String uri, boolean useSTAX) throws XPathException{
        Event event = createEvent();
        XPathResults results = new XPathResults(event, getDocumentXPathsCount(), expressions);
        sniff(event, uri, useSTAX);
        return results;
    }

    public void sniff(Event event, InputSource is) throws XPathException{
        if(docExpressions.size()>0)
            new SAXEngine(event, parser.langInterested).start(is);
    }

    public XPathResults sniff(InputSource is) throws XPathException{
        Event event = createEvent();
        XPathResults results = new XPathResults(event, getDocumentXPathsCount(), expressions);
        sniff(event, is);
        return results;
    }
}

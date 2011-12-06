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

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.dog.expr.EvaluationListener;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.expr.StaticEvaluation;
import jlibs.xml.sax.dog.expr.func.FunctionCall;
import jlibs.xml.sax.dog.expr.nodset.LocationExpression;
import jlibs.xml.sax.dog.expr.nodset.PathExpression;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.path.LocationPath;
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

    public XMLDog(NamespaceContext nsContext){
        this(nsContext, null, null);
    }

    public XMLDog(NamespaceContext nsContext, XPathVariableResolver variableResolver, XPathFunctionResolver functionResolver){
        this.nsContext = nsContext;
        this.variableResolver = variableResolver;
        this.functionResolver = functionResolver;
        parser = new XPathParser(nsContext, variableResolver, functionResolver);
    }

    private final List<Expression> expressions = new ArrayList<Expression>();
    private final List<Expression> docExpressions = new ArrayList<Expression>();
    private final List<Expression> globalExpressions = new ArrayList<Expression>();
    private final ArrayDeque<Expression> tempStack = new ArrayDeque<Expression>();

    public Expression addXPath(String xpath) throws SAXPathException{
        Expression compiledExpr = parser.parse(xpath, true);
        compiledExpr.setXPath(xpath);
        addXPath(compiledExpr);
        return compiledExpr;
    }

    public Expression addForEach(String forEach, String xpath) throws SAXPathException{
        Expression forEachExpr = parser.parse(forEach, true);
        LocationPath union = new LocationPath(Scope.LOCAL, 0);
        if(forEachExpr instanceof LocationExpression)
            union.addToContext(((LocationExpression)forEachExpr).locationPath);
        else
            union.addToContext(((PathExpression)forEachExpr).union);

        Expression relativeExpr = parser.parse(xpath, false);
        PathExpression compiledExpr = new PathExpression(union, relativeExpr, true);
        compiledExpr.setXPath("#for-each "+forEach+" #eval "+xpath);
        addXPath(compiledExpr);
        return compiledExpr;
    }

    @SuppressWarnings({"unchecked"})
    private void addXPath(Expression compiledExpr) throws SAXPathException{
        expressions.add(compiledExpr);

        switch(compiledExpr.scope()){
            case Scope.DOCUMENT:
                searchDocExpressions(compiledExpr, compiledExpr);
                break;
            case Scope.GLOBAL:
                assert compiledExpr instanceof Literal;
                globalExpressions.add(compiledExpr);
                break;
            default:
                throw new ImpossibleException("scope of "+compiledExpr.getXPath()+" can't be"+compiledExpr.scope());
        }
    }

    private void searchDocExpressions(Expression userExpr, Expression expr){
        if(expr.scope()==Scope.DOCUMENT){
            expr.id = docExpressions.size();
            docExpressions.add(expr);
            if(expr!=userExpr)
                expr.storeResult = true;
        }

        if(expr instanceof LocationExpression){
            for(Step step: ((LocationExpression)expr).locationPath.steps){
                Expression predicate = step.predicateSet.getPredicate();
                if(predicate!=null){
                    if(predicate.scope()!=Scope.GLOBAL)
                        searchDocExpressions(userExpr, predicate);
                    for(PositionalPredicate positionPredicate=step.predicateSet.headPositionalPredicate; positionPredicate!=null; positionPredicate=positionPredicate.next){
                        if(positionPredicate.predicate.scope()!=Scope.GLOBAL)
                            searchDocExpressions(userExpr, positionPredicate.predicate);
                    }
                }
            }
        }else if(expr instanceof FunctionCall){
            FunctionCall functionCall = (FunctionCall)expr;
            for(Expression member: functionCall.members){
                if(member.scope()!=Scope.GLOBAL)
                    searchDocExpressions(userExpr, member);
            }
        }else if(expr instanceof PathExpression){
            PathExpression pathExpr = (PathExpression)expr;
            if(pathExpr.union.predicateSet.getPredicate()!=null)
                searchDocExpressions(userExpr, pathExpr.union.predicateSet.getPredicate());
            for(Expression context: pathExpr.contexts)
                searchDocExpressions(userExpr, context);
            searchDocExpressions(userExpr, pathExpr.relativeExpression);
        }
    }

    public Iterable<Expression> getXPaths(){
        return expressions;
    }

    public int getDocumentXPathsCount(){
        return docExpressions.size();
    }

    public Event createEvent(){
        return new Event(nsContext, docExpressions, Constraint.ID_START+parser.constraints.size());
    }

    /*-------------------------------------------------[ Sniff ]---------------------------------------------------*/

    public void sniff(Event event, InputSource source, boolean useSTAX) throws XPathException{
        EvaluationListener listener = event.getListener();
        if(listener!=null){
            for(Expression expr: globalExpressions)
                listener.finished(new StaticEvaluation<Expression>(expr, -1, expr.getResult()));
        }
        if(!docExpressions.isEmpty()){
            if(useSTAX)
                new STAXEngine(event, parser.langInterested).start(source);
            else
                new SAXEngine(event, parser.langInterested).start(source);
        }
    }

    public XPathResults sniff(InputSource source, boolean useSTAX) throws XPathException{
        Event event = createEvent();
        XPathResults results = new XPathResults(event);
        event.setListener(results);
        sniff(event, source, useSTAX);
        return results;
    }

    public void sniff(Event event, InputSource source) throws XPathException{
        sniff(event, source, false);
    }

    public XPathResults sniff(InputSource source) throws XPathException{
        Event event = createEvent();
        XPathResults results = new XPathResults(event);
        event.setListener(results);
        sniff(event, source);
        return results;
    }
}

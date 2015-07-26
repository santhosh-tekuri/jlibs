/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.xml.sax.dog;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.SAXProperties;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.expr.func.FunctionCall;
import jlibs.xml.sax.dog.expr.nodset.LocationExpression;
import jlibs.xml.sax.dog.expr.nodset.PathExpression;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.path.PositionalPredicate;
import jlibs.xml.sax.dog.path.Step;
import jlibs.xml.sax.dog.sniff.Event;
import jlibs.xml.sax.dog.sniff.SAXHandler;
import jlibs.xml.sax.dog.sniff.XPathParser;
import jlibs.xml.stream.STAXXMLReader;
import org.jaxen.saxpath.SAXPathException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
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

    public boolean isAllowDefaultPrefixMapping(){
        return parser.isAllowDefaultPrefixMapping();
    }

    public void setAllowDefaultPrefixMapping(boolean allow){
        parser.setAllowDefaultPrefixMapping(allow);
    }

    private final List<Expression> expressions = new ArrayList<Expression>();
    private final List<Expression> docExpressions = new ArrayList<Expression>();
    private final List<Expression> globalExpressions = new ArrayList<Expression>();

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
        return new Event(nsContext, globalExpressions, docExpressions, Constraint.ID_START+parser.constraints.size(), parser.langInterested);
    }

    /*-------------------------------------------------[ Sniff ]---------------------------------------------------*/

    public void sniff(Event event, InputSource source, boolean useSTAX) throws XPathException{
        XMLReader reader;
        try{
            if(useSTAX)
                reader = new STAXXMLReader();
            else
                reader = SAXUtil.newSAXFactory(true, false, false).newSAXParser().getXMLReader();
            sniff(event, source, reader);
        }catch(Exception ex){
            throw new XPathException(ex);
        }
    }

    public void sniff(Event event, InputSource source, XMLReader reader) throws XPathException{
        try{
            SAXHandler handler = event.getSAXHandler();
            reader.setContentHandler(handler);
            reader.setProperty(SAXProperties.LEXICAL_HANDLER, handler);
        }catch(Exception ex){
            throw new XPathException(ex);
        }
        try{
            reader.parse(source);
        }catch(Exception ex){
            if(ex!=Event.STOP_PARSING)
                throw new XPathException(ex);
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

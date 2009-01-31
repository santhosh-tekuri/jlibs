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

import jlibs.core.lang.NotImplementedException;
import jlibs.core.lang.StringUtil;
import jlibs.xml.sax.sniff.model.*;
import jlibs.xml.sax.sniff.model.computed.*;
import jlibs.xml.sax.sniff.model.computed.derived.*;
import jlibs.xml.sax.sniff.model.computed.derived.nodeset.RelationalNodeSet;
import jlibs.xml.sax.sniff.model.computed.derived.nodeset.StringizedNodeSet;
import jlibs.xml.sax.sniff.model.computed.derived.nodeset.StringsNodeSet;
import jlibs.xml.sax.sniff.model.computed.derived.nodeset.SumNodeSet;
import org.jaxen.JaxenHandler;
import org.jaxen.expr.*;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.Operator;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class JaxenParser/* extends jlibs.core.graph.visitors.ReflectionVisitor<Object, Node>*/{
    private Root root;

    public JaxenParser(Root root){
        this.root = root;
    }

    public Node visit(Object elem)  throws SAXPathException{
        if(elem instanceof AllNodeStep)
            return process((AllNodeStep)elem);
        else if(elem instanceof NameStep)
            return process((NameStep)elem);
        else if(elem instanceof NumberExpr)
            return process((NumberExpr)elem);
        else if(elem instanceof org.jaxen.expr.Predicate)
            return process((org.jaxen.expr.Predicate)elem);
        else if(elem instanceof ProcessingInstructionNodeStep)
            return process((ProcessingInstructionNodeStep)elem);
        else if(elem instanceof FunctionCallExpr)
            return process((FunctionCallExpr)elem);
        else if(elem instanceof CommentNodeStep)
            return process((CommentNodeStep)elem);
        else if(elem instanceof LiteralExpr)
            return process((LiteralExpr)elem);
        else if(elem instanceof TextNodeStep)
            return process((TextNodeStep)elem);
        else if(elem instanceof LocationPath)
            return process((LocationPath)elem);
        else if(elem instanceof BinaryExpr)
            return process((BinaryExpr)elem);
        else
           throw new NotImplementedException(elem.getClass().getName());
    }

    protected Node getDefault(Object elem){
        throw new NotImplementedException(elem.getClass().getName());
    }

    private Node current;
    
    public XPath parse(String xpath) throws SAXPathException{
        XPathReader reader = XPathReaderFactory.createReader();
        JaxenHandler handler = new JaxenHandler();
        reader.setXPathHandler(handler);
        reader.parse(xpath);
        return parse(xpath, new XPathSimplifier().simplify(handler.getXPathExpr()));
    }

    public XPath parse(String xpath, XPathExpr xpathExpr) throws SAXPathException{
        current = root;
        visit(xpathExpr.getRootExpr());

        if(lastFilteredNodeSet!=null){
            lastFilteredNodeSet.userGiven = true;
            return new XPath(xpath, xpathExpr, lastFilteredNodeSet);
        }else{
            current.userGiven = true;
            return new XPath(xpath, xpathExpr, current);
        }
    }

    @SuppressWarnings({"unchecked"})
    protected Node process(LocationPath locationPath)  throws SAXPathException{
        if(locationPath.isAbsolute() || current==root || current.parent==null)
            current = root.addChild(new DocumentNode());

        boolean hasPredicate = false;
        for(Step step: (List<Step>)locationPath.getSteps()){
            visit(step);
            hasPredicate = step.getPredicates().size()>0;
        }

        if(lastFilteredNodeSet!=null){
            if(!hasPredicate)
                lastFilteredNodeSet = new FilteredNodeSet(current, lastFilteredNodeSet);
        }
        return current;
    }

    protected Node process(int axis) throws SAXPathException{
        if(axis!=Axis.SELF){
            AxisNode axisNode = AxisNode.newInstance(axis);
            boolean self = axis==Axis.DESCENDANT_OR_SELF || axis==Axis.ANCESTOR_OR_SELF;
            if(self)
                current = current.addConstraint(axisNode);
            else
                current = current.addChild(axisNode);
        }
        return current;
    }

    protected Node process(AllNodeStep allNodeStep) throws SAXPathException{
        return process(allNodeStep.getAxis());
    }

    protected Node process(TextNodeStep textNodeStep) throws SAXPathException{
        process(textNodeStep.getAxis());
        current = current.addConstraint(new Text());
        return current;
    }

    protected Node process(CommentNodeStep commentNodeStep) throws SAXPathException{
        process(commentNodeStep.getAxis());
        current = current.addConstraint(new Comment());
        return current;
    }

    protected Node process(ProcessingInstructionNodeStep piStep) throws SAXPathException{
        String name = piStep.getName();
        if(StringUtil.isEmpty(name)) // saxpath gives name="" for processing-instruction() i.e without argument
            name = null;

        process(piStep.getAxis());
        current = current.addConstraint(new ProcessingInstruction(name));
        return current;
    }

    protected Node process(NameStep nameStep) throws SAXPathException{
        process(nameStep.getAxis());

        String localName = nameStep.getLocalName();
        String prefix = nameStep.getPrefix();

        if(localName.equals("*"))
            localName = null;
        
        String uri = root.nsContext.getNamespaceURI(prefix);
        if(uri==null)
            throw new SAXPathException("undeclared prefix: "+prefix);
        
        if(StringUtil.isEmpty(uri) && localName==null)
            uri = null;

        current = current.addConstraint(new QNameNode(uri, localName));

        for(Object predicate: nameStep.getPredicates()){
            lastFilteredNodeSet = null;
            visit(predicate);
        }

        return current;
    }

    private FilteredNodeSet lastFilteredNodeSet;
    private int predicateDepth;
    protected Node process(org.jaxen.expr.Predicate p) throws SAXPathException{
        if(p.getExpr() instanceof NumberExpr){
            NumberExpr numberExpr = (NumberExpr)p.getExpr();
            current = current.addConstraint(new Position(numberExpr.getNumber().intValue()));
        }else{
            if(p.getExpr() instanceof EqualityExpr){
                EqualityExpr equalityExpr = (EqualityExpr)p.getExpr();
                if(equalityExpr.getLHS() instanceof FunctionCallExpr){
                    FunctionCallExpr function = (FunctionCallExpr)equalityExpr.getLHS();
                    if(function.getPrefix().equals("") && function.getFunctionName().equals("position")){
                        if(equalityExpr.getRHS() instanceof NumberExpr){
                            NumberExpr numberExpr = (NumberExpr)equalityExpr.getRHS();
                            current = current.addConstraint(new Position(numberExpr.getNumber().intValue()));
                            return current;                            
                        }
                    }
                }
            }
            predicateDepth++;
            Node context = current;
            visit(p.getExpr());


            Node filter = lastFilteredNodeSet==null ? current : lastFilteredNodeSet;
            FilteredNodeSet filteredNodeSet;
            if(predicateDepth==1)
                filteredNodeSet = new ContextSensitiveFilteredNodeSet(context, filter);
            else
                filteredNodeSet = new FilteredNodeSet(context, filter);

            lastFilteredNodeSet = filteredNodeSet;

            predicateDepth--;
            current = context;
        }

        return current;
    }

    @SuppressWarnings({"unchecked"})
    protected Node process(FunctionCallExpr functionExpr) throws SAXPathException{
        String prefix = functionExpr.getPrefix();
        String name = functionExpr.getFunctionName();

        if(prefix.length()>0)
            throw new SAXPathException("unsupported function "+prefix+':'+name+"()");

        ComputedResults function = null;

        if(name.equals("strings"))
            function = new StringsNodeSet();
        else if(name.equals("count"))
            function = new Count();
        else if(name.equals("name"))
            function = new QualifiedName();
        else if(name.equals("local-name"))
            function = new LocalName();
        else if(name.equals("namespace-uri"))
            function = new NamespaceURI();
        else if(name.equals("string"))
            function = new StringizedNodeSet();
        else if(name.equals("sum"))
            function = new SumNodeSet();
        else if(name.equals("concat"))
            function = new Concat();
        else if(name.equals("normalize-space"))
            function = new NormalizeSpace();
        else if(name.equals("string-length"))
            function = new StringLength();
        else if(name.equals("number"))
            function = new ToNumber();
        else if(name.equals("boolean"))
            function = new BooleanizedNodeSet();
        else if(name.equals("not"))
            function = new Not();

        if(function!=null){
            for(Expr param: (List<Expr>)functionExpr.getParameters()){
                current = visit(param);
                function.addMember(current, lastFilteredNodeSet);
            }
            current = function;
        }else{
            if(functionExpr.getFunctionName().equals("true"))
                current = new Literal(root, true);
            else if(functionExpr.getFunctionName().equals("false"))
                current = new Literal(root, false);
            else
                throw new NotImplementedException("function "+name+"() is not supported");
        }
        
        lastFilteredNodeSet = null;
        return current;
    }

    protected Node process(LiteralExpr literalExpr) throws SAXPathException{
        current = new Literal(root, literalExpr.getLiteral());
        return current;
    }

    protected Node process(NumberExpr numberExpr) throws SAXPathException{
        current = new Literal(root, numberExpr.getNumber().doubleValue());
        return current;
    }

    protected Node process(BinaryExpr binaryExpr) throws SAXPathException{
        ComputedResults computed = null;
        if(binaryExpr.getOperator().equals("and"))
            computed = new AndExpression();
        else if(binaryExpr.getOperator().equals("or"))
            computed = new OrExpression();
        else if(binaryExpr.getOperator().equals("="))
            computed = new RelationalNodeSet();
        else{
            int operator = -1;
            if(binaryExpr.getOperator().equals("+"))
                operator = Operator.ADD;
            else if(binaryExpr.getOperator().equals("-"))
                operator = Operator.SUBTRACT;
            else if(binaryExpr.getOperator().equals("*"))
                operator = Operator.MULTIPLY;
            else if(binaryExpr.getOperator().equals("div"))
                operator = Operator.DIV;
            else if(binaryExpr.getOperator().equals("mod"))
                operator = Operator.MOD;
            if(operator!=-1)
                computed = new ArithmeticExpression(operator);
        }
        if(computed!=null){
            Node _current = current;
            visit(binaryExpr.getLHS());
            computed.addMember(current, lastFilteredNodeSet);

            current = _current;
            visit(binaryExpr.getRHS());
            computed.addMember(current, lastFilteredNodeSet);

            return current = computed;
        }else
            throw new SAXPathException("unsupported operator: "+binaryExpr.getOperator());
    }

//    public static void main(String[] args) throws SAXPathException{
//        new JaxenParser(null).generateCode();
//    }
}

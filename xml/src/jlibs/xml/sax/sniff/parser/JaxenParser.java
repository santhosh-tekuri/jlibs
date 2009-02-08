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

package jlibs.xml.sax.sniff.parser;

import jlibs.core.lang.NotImplementedException;
import jlibs.core.lang.StringUtil;
import jlibs.xml.sax.sniff.XPath;
import jlibs.xml.sax.sniff.model.*;
import jlibs.xml.sax.sniff.model.expr.Expression;
import jlibs.xml.sax.sniff.model.expr.Literal;
import jlibs.xml.sax.sniff.model.expr.TypeCast;
import jlibs.xml.sax.sniff.model.expr.bool.*;
import jlibs.xml.sax.sniff.model.expr.num.Arithmetic;
import jlibs.xml.sax.sniff.model.expr.string.*;
import org.jaxen.JaxenHandler;
import org.jaxen.expr.*;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.Operator;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;

import java.util.ArrayDeque;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class JaxenParser/* extends jlibs.core.graph.visitors.ReflectionVisitor<Object, Node>*/{
    private Root root;

    public JaxenParser(Root root){
        this.root = root;
    }

    public Notifier visit(Object elem)  throws SAXPathException{
        if(elem instanceof org.jaxen.expr.LocationPath)
            return process((org.jaxen.expr.LocationPath)elem);
        else if(elem instanceof Step)
            return process((Step)elem);
        else if(elem instanceof NumberExpr)
            return process((NumberExpr)elem);
        else if(elem instanceof Predicate)
            return process((Predicate)elem);
        else if(elem instanceof FunctionCallExpr)
            return process((FunctionCallExpr)elem);
        else if(elem instanceof LiteralExpr)
            return process((LiteralExpr)elem);
        else if(elem instanceof BinaryExpr)
            return process((BinaryExpr)elem);
        else
           throw new NotImplementedException(elem.getClass().getName());
    }

    protected Notifier getDefault(Object elem){
        throw new NotImplementedException(elem.getClass().getName());
    }

    private Notifier current;
    private ArrayDeque<Node> contextStack = new ArrayDeque<Node>();
    private ArrayDeque<LocationPath> locationStack = new ArrayDeque<LocationPath>();
    private LocationPath location;

    public XPath parse(String xpath) throws SAXPathException{
        XPathReader reader = XPathReaderFactory.createReader();
        JaxenHandler handler = new JaxenHandler();
        reader.setXPathHandler(handler);
        reader.parse(xpath);
        return parse(xpath, new XPathSimplifier().simplify(handler.getXPathExpr()));
    }

    public XPath parse(String xpath, XPathExpr jaxenExpr) throws SAXPathException{
        current = root;
        contextStack.add(root);
        visit(jaxenExpr.getRootExpr());

        Expression expr;
        if(current instanceof Expression)
            expr = (Expression)current;
        else
            expr = location.create(root, Datatype.NODESET);
        
        return new XPath(xpath, jaxenExpr, expr);
    }

    /*-------------------------------------------------[ LocationPath ]---------------------------------------------------*/
    
    @SuppressWarnings({"unchecked"})
    protected Notifier process(org.jaxen.expr.LocationPath locationPath)  throws SAXPathException{
        if(current==root || (locationPath.isAbsolute() && !(current instanceof DocumentNode)))
            current = root.addChild(new DocumentNode());

        locationStack.push(location=new LocationPath());
        for(Step step: (List<Step>)locationPath.getSteps())
            visit(step);
        location = locationStack.pop();

        return current;
    }

    /*-------------------------------------------------[ Step ]---------------------------------------------------*/
    
    protected Notifier process(int axis) throws SAXPathException{
        if(axis!=Axis.SELF){
            AxisNode axisNode = AxisNode.newInstance(axis);
            boolean self = axis==Axis.DESCENDANT_OR_SELF || axis==Axis.ANCESTOR_OR_SELF;
            if(self)
                current = ((Node)current).addConstraint(axisNode);
            else
                current = ((Node)current).addChild(axisNode);
        }
        return current;
    }

    protected Notifier process(Step step) throws SAXPathException{
        current = process(step.getAxis());

        Node constraint = null;
        if(step instanceof TextNodeStep)
            constraint = new Text();
        else if(step instanceof CommentNodeStep)
            constraint = new Comment();
        else if(step instanceof ProcessingInstructionNodeStep){
            ProcessingInstructionNodeStep piStep = (ProcessingInstructionNodeStep)step;

            String name = piStep.getName();
            if(StringUtil.isEmpty(name)) // saxpath gives name="" for processing-instruction() i.e without argument
                name = null;
            constraint = new ProcessingInstruction(name);
        }else if(step instanceof NameStep){
            NameStep nameStep = (NameStep)step;

            String localName = nameStep.getLocalName();
            String prefix = nameStep.getPrefix();

            if(localName.equals("*"))
                localName = null;

            String uri = root.nsContext.getNamespaceURI(prefix);
            if(uri==null)
                throw new SAXPathException("undeclared prefix: "+prefix);

            if(StringUtil.isEmpty(uri) && localName==null)
                uri = null;

            constraint = new QNameNode(uri, localName);
        }

        if(constraint!=null)
            current = ((Node)current).addConstraint(constraint);

        locationStack.peek().addStep((Node)current);
        for(Object predicate: step.getPredicates())
            visit(predicate);

        return current;
    }

    /*-------------------------------------------------[ Predicate ]---------------------------------------------------*/

    protected Notifier process(Predicate p) throws SAXPathException{
        if(p.getExpr() instanceof NumberExpr){
            NumberExpr numberExpr = (NumberExpr)p.getExpr();
            current = ((Node)current).addConstraint(new Position(numberExpr.getNumber().intValue()));
            locationStack.peek().setStep((Node)current);
        }else{
            if(p.getExpr() instanceof EqualityExpr){
                EqualityExpr equalityExpr = (EqualityExpr)p.getExpr();
                if(equalityExpr.getLHS() instanceof FunctionCallExpr){
                    FunctionCallExpr function = (FunctionCallExpr)equalityExpr.getLHS();
                    if(function.getPrefix().equals("") && function.getFunctionName().equals("position")){
                        if(equalityExpr.getRHS() instanceof NumberExpr){
                            NumberExpr numberExpr = (NumberExpr)equalityExpr.getRHS();
                            current = ((Node)current).addConstraint(new Position(numberExpr.getNumber().intValue()));
                            locationStack.peek().setStep((Node)current);
                            return current;
                        }
                    }
                }
            }
            contextStack.push((Node)current);
            visit(p.getExpr());

            applyLocation(Datatype.BOOLEAN);
            locationStack.peek().setPredicate((Expression)current);
            current = contextStack.pop();
        }

        return current;
    }

    /*-------------------------------------------------[ Functions ]---------------------------------------------------*/

    private void applyLocation(Datatype expected){
        if(!(current instanceof Expression)){
            if(current.resultType()!=expected)
                current = location.create(contextStack.peek(), expected);
        }
    }

    private void addMember(Expression function, Expr member) throws SAXPathException{
        Notifier _current = current;
        current = visit(member);
        applyLocation(function.memberType());
        function.addMember(current);
        current = _current;
    }
    
    @SuppressWarnings({"unchecked"})
    protected Notifier process(FunctionCallExpr functionExpr) throws SAXPathException{
        String prefix = functionExpr.getPrefix();
        String name = functionExpr.getFunctionName();

        if(prefix.length()>0)
            throw new SAXPathException("unsupported function "+prefix+':'+name+"()");

        if(functionExpr.getFunctionName().equals("true"))
            return current = new Literal(contextStack.peek(), true);
        else if(functionExpr.getFunctionName().equals("false"))
            return current = new Literal(contextStack.peek(), false);

        Expression function = null;

        Node context = contextStack.peek();
        if(functionExpr.getParameters().size()>0){
            visit(functionExpr.getParameters().get(0));
            if(!(current instanceof Expression) && location!=null){
                Notifier f = location.createFunction(context, name);
                if(f!=null)
                    return current = f;
            }
        }else{
            Notifier f = new LocationPath().createFunction(context, name);
            if(f!=null)
                return current = f;
        }

        if(name.equals("number"))
            function = new TypeCast(contextStack.peek(), Datatype.NUMBER);
        else if(name.equals("boolean"))
            function = new TypeCast(contextStack.peek(), Datatype.BOOLEAN);
        else if(name.equals("string-length"))
            function = new StringLength(contextStack.peek());
        else if(name.equals("concat"))
            function = new Concat(contextStack.peek());
        else if(name.equals("not"))
            function = new Not(contextStack.peek());
        else if(name.equals("normalize-space"))
            function = new NormalizeSpace(contextStack.peek());
        else if(name.equals("translate"))
            function = new Translate(contextStack.peek());
        else if(name.equals("contains"))
            function = new Contains(contextStack.peek());
        else if(name.equals("starts-with"))
            function = new StartsWith(contextStack.peek());
        else if(name.equals("substring"))
            function = new Substring(contextStack.peek());

        if(function==null)
            throw new NotImplementedException("Function "+name+" is not implemented yet");
        
        int i = 0;
        for(Expr param: (List<Expr>)functionExpr.getParameters()){
            if(i!=0)
                current = visit(param);
            applyLocation(function.memberType(i));
            function.addMember(current);
            i++;
        }

        return current = function;
    }

    protected Notifier process(LiteralExpr literalExpr) throws SAXPathException{
        return current = new Literal(contextStack.peek(), literalExpr.getLiteral());
    }

    protected Notifier process(NumberExpr numberExpr) throws SAXPathException{
        return current = new Literal(contextStack.peek(), numberExpr.getNumber().doubleValue());
    }

    protected Notifier process(BinaryExpr binaryExpr) throws SAXPathException{
        Expression expr = null;
        if(binaryExpr.getOperator().equals("and"))
            expr = new AndOr(contextStack.peek(), false);
        else if(binaryExpr.getOperator().equals("or"))
            expr = new AndOr(contextStack.peek(), true);
        else if(binaryExpr.getOperator().equals("="))
            expr = new Equals(contextStack.peek());
        else if(binaryExpr.getOperator().equals("!="))
            expr = new NotEquals(contextStack.peek());
        else if(binaryExpr.getOperator().equals("<"))
            expr = new LessThan(contextStack.peek());
        else if(binaryExpr.getOperator().equals("<="))
            expr = new LessThanEqual(contextStack.peek());
        else if(binaryExpr.getOperator().equals(">"))
            expr = new GreaterThan(contextStack.peek());
        else if(binaryExpr.getOperator().equals(">="))
            expr = new GreaterThanEqual(contextStack.peek());
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
                expr = new Arithmetic(contextStack.peek(), operator);
        }
        if(expr!=null){
            addMember(expr, binaryExpr.getLHS());
            addMember(expr, binaryExpr.getRHS());
            return current = expr;
        }else
            throw new SAXPathException("unsupported operator: "+binaryExpr.getOperator());
    }

//    public static void main(String[] args) throws SAXPathException{
//        new JaxenParser(null).generateCode();
//    }
}

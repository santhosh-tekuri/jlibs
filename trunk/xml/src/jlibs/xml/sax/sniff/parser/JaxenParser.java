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
import jlibs.xml.sax.sniff.model.expr.*;
import jlibs.xml.sax.sniff.model.expr.bool.*;
import jlibs.xml.sax.sniff.model.expr.num.Arithmetic;
import jlibs.xml.sax.sniff.model.expr.num.Ceiling;
import jlibs.xml.sax.sniff.model.expr.num.Floor;
import jlibs.xml.sax.sniff.model.expr.num.Round;
import jlibs.xml.sax.sniff.model.expr.string.*;
import org.jaxen.JaxenHandler;
import org.jaxen.expr.*;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.Operator;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;

import javax.xml.namespace.QName;
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
        else if(elem instanceof UnaryExpr)
            return process((UnaryExpr)elem);
        else if(elem instanceof BinaryExpr)
            return process((BinaryExpr)elem);
        else if(elem instanceof VariableReferenceExpr)
            return process((VariableReferenceExpr)elem);
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
        else{
            UnionPath union = unionStack.peek();
            if(union==null)
                expr = location.create(Datatype.NODESET);
            else
                expr = union.create(Datatype.NODESET);
        }
        
        return new XPath(xpath, jaxenExpr, expr);
    }

    /*-------------------------------------------------[ LocationPath ]---------------------------------------------------*/
    
    @SuppressWarnings({"unchecked"})
    protected Notifier process(org.jaxen.expr.LocationPath locationPath)  throws SAXPathException{
        if(current==root || (locationPath.isAbsolute() && !(current instanceof DocumentNode)))
            current = root.addChild(new DocumentNode());

        locationStack.push(location=new LocationPath((Node)current));
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
        contextStack.push((Node)current);
        visit(p.getExpr());

        applyLocation(Datatype.BOOLEAN);
        locationStack.peek().setPredicate((Expression)current);
        return current = contextStack.pop();
    }

    /*-------------------------------------------------[ Functions ]---------------------------------------------------*/

    private void applyLocation(Datatype expected){
        if(!(current instanceof Expression)){
            if(current.resultType()!=expected)
                current = location.create(expected);
        }
    }

    private void addMember(Expression function, Expr member) throws SAXPathException{
        Notifier _current = current;
        current = visit(member);
        applyLocation(function.memberType());
        function.addMember(current);
        current = _current;
    }
    
    private Expression createFunction(String uri, String name) throws SAXPathException{
        if(uri.length()==0){
            if(name.equals("number"))
                return new TypeCast(contextStack.peek(), Datatype.NUMBER);
            else if(name.equals("boolean"))
                return new TypeCast(contextStack.peek(), Datatype.BOOLEAN);
            else if(name.equals("string-length"))
                return new StringLength(contextStack.peek());
            else if(name.equals("concat"))
                return new Concat(contextStack.peek());
            else if(name.equals("not"))
                return new Not(contextStack.peek());
            else if(name.equals("normalize-space"))
                return new NormalizeSpace(contextStack.peek());
            else if(name.equals("translate"))
                return new Translate(contextStack.peek());
            else if(name.equals("contains"))
                return new Contains(contextStack.peek());
            else if(name.equals("starts-with"))
                return new StartsWith(contextStack.peek());
            else if(name.equals("ends-with"))
                return new EndsWith(contextStack.peek());
            else if(name.equals("upper-case"))
                return new UpperCase(contextStack.peek());
            else if(name.equals("lower-case"))
                return new LowerCase(contextStack.peek());
            else if(name.equals("substring"))
                return new Substring(contextStack.peek());
            else if(name.equals("lang"))
                return new LanguageMatch(contextStack.peek());
            else if(name.equals("round"))
                return new Round(contextStack.peek());
            else if(name.equals("floor"))
                return new Floor(contextStack.peek());
            else if(name.equals("ceiling"))
                return new Ceiling(contextStack.peek());
        }

        if(root.functionResolver==null)
            throw new SAXPathException("Function Resolver is not set");
        return new UserFunction(contextStack.peek(), new QName(uri, name));
    }

    @SuppressWarnings({"unchecked"})
    protected Notifier process(FunctionCallExpr functionExpr) throws SAXPathException{
        String prefix = functionExpr.getPrefix();
        String name = functionExpr.getFunctionName();

        String uri = root.nsContext.getNamespaceURI(prefix);
        if(uri==null)
            throw new SAXPathException("undeclared prefix: "+prefix);

        if(uri.length()==0){
            if(functionExpr.getFunctionName().equals("true"))
                return current = new Literal(contextStack.peek(), true);
            else if(functionExpr.getFunctionName().equals("false"))
                return current = new Literal(contextStack.peek(), false);
        }

        Expression function = null;

        Node context = contextStack.peek();
        if(uri.length()==0){
            if(functionExpr.getParameters().size()>0){
                visit(functionExpr.getParameters().get(0));
                if(!(current instanceof Expression)){
                    if(unionStack.peek()!=null){
                        Notifier f = unionStack.peek().createFunction(name);
                        if(f!=null)
                            return current = f;
                    }else if(location!=null){
                        Notifier f = location.createFunction(name);
                        if(f!=null)
                            return current = f;
                    }
                }
            }else{
                LocationPath loc = locationStack.isEmpty() ? new LocationPath(context) : locationStack.peek();
                Notifier f = loc.createFunctionWithLastPredicate(name);
                if(f!=null)
                    return current = f;
            }
        }

        function = createFunction(uri, name);
        if(uri.length()==0 && name.equals("lang"))
            function.addMember(new LocationPath(context).createFunction("lang"));
        
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

    protected Notifier process(UnaryExpr unaryExpr) throws SAXPathException{
        Expression expr = new Arithmetic(contextStack.peek(), Operator.MULTIPLY);
        expr.addMember(new Literal(contextStack.peek(), (double)-1));
        addMember(expr, unaryExpr.getExpr());
        return current = expr;
    }

    protected Notifier process(VariableReferenceExpr varRefExpr) throws SAXPathException{
        if(root.variableResolver==null)
            throw new SAXPathException("Variable Resolver is not set");

        String uri = root.nsContext.getNamespaceURI(varRefExpr.getPrefix());
        if(uri==null)
            throw new SAXPathException("undeclared prefix: "+varRefExpr.getPrefix());

        QName qname = new QName(uri, varRefExpr.getVariableName());
        return current = new VariableReference(contextStack.peek(), qname);
    }

    private ArrayDeque<UnionPath> unionStack = new ArrayDeque<UnionPath>();
    
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
        }else if(binaryExpr.getOperator().equals("|")){
            Notifier _current = current;
            UnionPath union = new UnionPath(contextStack.peek());

            int size = unionStack.size();
            visit(binaryExpr.getLHS());
            if(unionStack.size()==size)
                union.setLHS(location);
            else
                union.setLHS(unionStack.peek());

            visit(binaryExpr.getRHS());
            if(unionStack.size()==size)
                union.setRHS(location);
            else
                union.setRHS(unionStack.peek());
            
            unionStack.push(union);
            return _current;
        }else
            throw new SAXPathException("unsupported operator: "+binaryExpr.getOperator());
    }

//    public static void main(String[] args) throws SAXPathException{
//        new JaxenParser(null).generateCode();
//    }
}

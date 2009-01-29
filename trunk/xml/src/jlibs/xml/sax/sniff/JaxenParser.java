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
import jlibs.xml.sax.sniff.model.functions.*;
import jlibs.xml.sax.sniff.model.listeners.ArithmeticOperation;
import jlibs.xml.sax.sniff.model.listeners.DerivedResults;
import jlibs.xml.sax.sniff.model.listeners.LogicalOperation;
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
                lastFilteredNodeSet = new FilteredNodeSet(current, _toBoolean(lastFilteredNodeSet));
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

            FilteredNodeSet filteredNodeSet = null;
            if(lastFilteredNodeSet==null)
                filteredNodeSet = new FilteredNodeSet(context, _toBoolean(current));
            else
                filteredNodeSet = new FilteredNodeSet(context, lastFilteredNodeSet);
            
            lastFilteredNodeSet = filteredNodeSet;
            predicateDepth--;
            if(predicateDepth==0)
                filteredNodeSet.contextSensitive();
            current = context;
        }

        return current;
    }

    private DerivedResults createDerivedResults(String name){
        if(name.equals("normalize-space"))
            return new NormalizeSpace();
        else if(name.equals("string-length"))
            return new StringLength();
        else if(name.equals("concat"))
            return new Concat();
        else if(name.equals("boolean"))
            return new Booleanize();
        else if(name.equals("number"))
            return new NumberFunction();
        else
            return null;
    }

    protected Node process(FunctionCallExpr functionExpr) throws SAXPathException{
        String prefix = functionExpr.getPrefix();
        String name = functionExpr.getFunctionName();

        if(prefix.length()>0)
            throw new SAXPathException("unsupported function "+prefix+':'+name+"()");

        if(name.equals("count")){
            current = visit(functionExpr.getParameters().get(0));
            if(lastFilteredNodeSet!=null)
                current = new Count(lastFilteredNodeSet);
            else
                current = new Count(current);
        }else if(name.equals("name")){
            current = visit(functionExpr.getParameters().get(0));
            if(lastFilteredNodeSet!=null)
                current = new QualifiedName(lastFilteredNodeSet);
            else
                current = new QualifiedName(current);
        }else if(name.equals("local-name")){
            current = visit(functionExpr.getParameters().get(0));
            if(lastFilteredNodeSet!=null)
                current = new LocalName(lastFilteredNodeSet);
            else
                current = new LocalName(current);
        }else if(name.equals("namespace-uri")){
            current = visit(functionExpr.getParameters().get(0));
            if(lastFilteredNodeSet!=null)
                current = new NamespaceURI(lastFilteredNodeSet);
            else
                current = new NamespaceURI(current);
        }else{
            DerivedResults derivedResults = createDerivedResults(name);
            if(derivedResults!=null){
                for(Object parameter: functionExpr.getParameters()){
                    visit(parameter);
                    if(name.equals("boolean") && current.resultType()==ResultType.NODESET)
                        current = new BooleanizedNodeSet(current);
                    else{
                        if(!name.equals("boolean") && current.resultType()==ResultType.NODESET)
                            current = current.addConstraint(new StringFunction());
                        derivedResults.addMember(current);
                    }
                }
                if(!(current instanceof ComputedResults))
                    current = root.addConstraint(derivedResults.attach());
            }else if(functionExpr.getFunctionName().equals("true"))
                current = root.addConstraint(new BooleanNode(true));
            else if(functionExpr.getFunctionName().equals("false"))
                current = root.addConstraint(new BooleanNode(false));
            else{
                Function function = Function.newInstance(functionExpr.getFunctionName());
                for(Object parameter: functionExpr.getParameters())
                    visit(parameter);

    //            if(lastFilteredNodeSet!=null)
    //                current = lastFilteredNodeSet.addConstraint(function);
    //            else
                    current = current.addConstraint(function);
            }
        }
        lastFilteredNodeSet = null;
        return current;
    }

    protected Node process(LiteralExpr literalExpr) throws SAXPathException{
        current = root.addConstraint(new LiteralNode(literalExpr.getLiteral()));
        return current;
    }

    protected Node process(NumberExpr numberExpr) throws SAXPathException{
        current = root.addConstraint(new NumberNode(numberExpr.getNumber().doubleValue()));
        return current;
    }

    protected Node process(BinaryExpr binaryExpr) throws SAXPathException{
        if(binaryExpr.getOperator().equals("and")){
            Node _current = current;
            visit(binaryExpr.getLHS());
            Node lhs = current;

            current = _current;
            visit(binaryExpr.getRHS());
            Node rhs = current;

            return current = new AndExpression(lhs, rhs);
        }else if(binaryExpr.getOperator().equals("or")){
            Node _current = current;
            visit(binaryExpr.getLHS());
            Node lhs = current;

            current = _current;
            visit(binaryExpr.getRHS());
            Node rhs = current;

            return current = new OrExpression(lhs, rhs);
        }

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

        DerivedResults derivedResults = null;
        if(operator!=-1)
            derivedResults = new ArithmeticOperation(operator);
        else if(binaryExpr.getOperator().equals("and"))
            derivedResults = new LogicalOperation(true);
        else if(binaryExpr.getOperator().equals("or"))
            derivedResults = new LogicalOperation(false);
        else
            throw new SAXPathException("unsupported operator: "+binaryExpr.getOperator());

        Node _current = current;
        visit(binaryExpr.getLHS());
        if(derivedResults.resultType()==ResultType.BOOLEAN)
            current = toBoolean(current);
        derivedResults.addMember(current);

        current = _current;
        visit(binaryExpr.getRHS());
        if(derivedResults.resultType()==ResultType.BOOLEAN)
            current = toBoolean(current);
        derivedResults.addMember(current);
        current = root.addConstraint(derivedResults.attach());

        return current;
    }

    /*-------------------------------------------------[ DataConvertion ]---------------------------------------------------*/

    public UserResults _toBoolean(Node node){
        return node;
//        if(node.resultType()==ResultType.BOOLEAN)
//            return node;
//        else if(node.resultType()==ResultType.NODESET){
//            return new BooleanizedNodeSet(node);
//        }else
//            throw new NotImplementedException("toBoolean for "+node.resultType()+" is not implemented");
    }

    public Node toBoolean(Node node){
        if(node.resultType()==ResultType.BOOLEAN)
            return node;
        else if(node.resultType()==ResultType.NODESET){
            Function function = new BooleanFunction();
            return node.addConstraint(function);
        }else{
            DerivedResults derivedResults = new Booleanize();
            derivedResults.addMember(node);
            derivedResults = derivedResults.attach();
            root.addConstraint(derivedResults);
            return derivedResults;
        }
    }

//    public static void main(String[] args) throws SAXPathException{
//        new JaxenParser(null).generateCode();
//    }
}

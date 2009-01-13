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
import jlibs.core.lang.Util;
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;
import jlibs.xml.sax.sniff.model.*;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathHandler;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.helpers.XPathReaderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;
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
    private List<Node> currents = new ArrayList<Node>();    
    public XPath parse(String xpath) throws SAXPathException{
        if(reader==null){
            reader = XPathReaderFactory.createReader();
            reader.setXPathHandler(this);
        }
        currents.clear();
        currents.add(root);
        reader.parse(xpath);

        return predicate!=null ? new XPath(xpath, predicate) : new XPath(xpath, currents);
    }

    @Override
    public void startXPath() throws SAXPathException{}

    @Override
    public void endXPath() throws SAXPathException{
        if(predicate!=null){
            predicate = new Predicate(predicate);
            predicate.userGiven = true;
            for(Node current: currents)
                current.predicates.add(predicate);
        }else{
            for(Node node: currents)
                node.userGiven = true;
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
        currents.clear();
        currents.add(root);
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

        if(uri==null/* && localName==null*/) // localName is always null: idea is intelligent
            return;

        List<Node> newCurrents = new ArrayList<Node>();
        for(Node current: currents){
            QNameNode found = null;
            for(Node child: current.constraints){
                if(child instanceof QNameNode){
                    QNameNode qname = (QNameNode)child;
                    if(Util.equals(uri, qname.uri) && Util.equals(localName, qname.name)){
                        found = qname;
                        break;
                    }
                }
            }
            if(found==null)
                found = new QNameNode(current, uri, localName);
            newCurrents.add(found);
        }
        currents = newCurrents;
    }

    @Override
    public void endNameStep() throws SAXPathException{}

    @Override
    public void startTextNodeStep(int axis) throws SAXPathException{
        startAllNodeStep(axis);

        List<Node> newCurrents = new ArrayList<Node>();
        for(Node current: currents){
            Text found = null;
            for(Node child: current.constraints){
                if(child instanceof Text){
                    found = (Text)child;
                    break;
                }
            }
            if(found==null)
                found = new Text(current);
            newCurrents.add(found);
        }
        currents = newCurrents;
    }

    @Override
    public void endTextNodeStep() throws SAXPathException{}

    @Override
    public void startCommentNodeStep(int axis) throws SAXPathException{
        throw new SAXPathException("comment node is unsupprted");
    }

    @Override
    public void endCommentNodeStep() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void startAllNodeStep(int axis) throws SAXPathException{
        List<Node> newCurrents = new ArrayList<Node>();
        if(axis==Axis.DESCENDANT_OR_SELF){
            axis = Axis.DESCENDANT;
            newCurrents.addAll(currents);
        }
        for(Node current: currents){
            AxisNode found = null;
            for(AxisNode child: current.children){
                if(child.type==axis){
                    found = child;
                    break;
                }
            }
            if(found==null)
                found = AxisNode.newInstance(current, axis);
            newCurrents.add(found);
        }
        currents = newCurrents;
    }

    @Override
    public void endAllNodeStep() throws SAXPathException{}

    @Override
    public void startProcessingInstructionNodeStep(int axis, String name) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void endProcessingInstructionNodeStep() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    private Deque<List<Node>> predicates = new ArrayDeque<List<Node>>();
    private ArrayDeque<Integer> pathStack = new ArrayDeque<Integer>();

    @Override
    public void startPredicate() throws SAXPathException{
        predicates.push(new ArrayList<Node>(currents));
        pathStack.push(0);
    }

    private Predicate predicate;
    @Override
    public void endPredicate() throws SAXPathException{
        if(pathStack.pop()>0){
            List<Node> list = predicates.pop();
            predicate = new Predicate(currents.toArray(new Node[currents.size()]));
            for(Node node: list)
                node.predicates.add(predicate);
            currents = list;
        }else
            predicates.pop();

    }

    @Override
    public void startFilterExpr() throws SAXPathException{
//        throw new SAXPathException("filter expression is unsupprted");
    }

    @Override
    public void endFilterExpr() throws SAXPathException{
//        throw new SAXPathException("unsupprted");
    }

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
        if(predicates.size()>0){
            List<Node> newCurrents = new ArrayList<Node>();
            for(Node current: currents){
                Position found = null;
                for(Node child: current.constraints){
                    if(child instanceof Position){
                        Position position = (Position)child;
                        if(position.pos==number){
                            found = position;
                            break;
                        }
                    }
                }
                if(found==null)
                    found = new Position(current, number);
                newCurrents.add(found);
            }
            currents = newCurrents;
        }else
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

    @Override
    public void startFunction(String prefix, String functionName) throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void endFunction() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    public static void main(String[] args) throws SAXPathException{
        DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
        nsContext.declarePrefix("xsd", Namespaces.URI_XSD);
        nsContext.declarePrefix("xs", Namespaces.URI_XSD);

        Root root = new Root(nsContext);
        XPathParser parser = new XPathParser(root);

        String xpaths[] = {
//            "//xsd:any/@namespace",
//            "//@name",
//            "a/b",
//            "/xsd:schema//xsd:complexType/@name",
            "/xsd:schema/xsd:any[xyz/qwe[abc/def]]/mno",
//            "/xsd:sequence/xsd:any[2]/xs:element/@namespace",
        };

        for(String xpath: xpaths){
            XPath xp = parser.parse(xpath);
            System.out.println(xp.xpath+"\n");
            root.print();
            System.out.println("-----------------------------------------------------------");            
        }
    }
}

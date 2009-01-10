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

import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;
import org.jaxen.saxpath.Axis;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.XPathHandler;
import org.jaxen.saxpath.XPathReader;
import org.jaxen.saxpath.helpers.XPathReaderFactory;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * @author Santhosh Kumar T
 */
public class XPathParser implements XPathHandler{
    private NamespaceContext nsContext;

    public XPathParser(NamespaceContext nsContext){
        this.nsContext = nsContext;
    }

    private XPathReader reader;
    private Node current;
    public Node parse(String xpath) throws SAXPathException{
        if(reader==null){
            reader = XPathReaderFactory.createReader();
            reader.setXPathHandler(this);
        }
        reader.parse(xpath);
        return current;
    }

    @Override
    public void startXPath() throws SAXPathException{
        current = new Root(nsContext);
    }

    @Override
    public void endXPath() throws SAXPathException{
    }

    @Override
    public void startPathExpr() throws SAXPathException{}

    @Override
    public void endPathExpr() throws SAXPathException{}

    @Override
    public void startAbsoluteLocationPath() throws SAXPathException{}

    @Override
    public void endAbsoluteLocationPath() throws SAXPathException{}

    @Override
    public void startRelativeLocationPath() throws SAXPathException{
        throw new SAXPathException("relative location path is unsupprted");
    }

    @Override
    public void endRelativeLocationPath() throws SAXPathException{
        throw new SAXPathException("unsupprted");
    }

    @Override
    public void startNameStep(int axis, String prefix, String localName) throws SAXPathException{
        QName qname = null;
        String namespace = null;
        if(!localName.equals("*"))
            qname = new QName(nsContext.getNamespaceURI(prefix), localName, prefix);
        else if(prefix.length()>0)
            namespace = nsContext.getNamespaceURI(prefix); 

        switch(axis){
            case Axis.CHILD:
                current = new Element(current, qname, namespace);
                break;
            case Axis.ATTRIBUTE:
                current = new Attribute(current, qname, namespace);
                break;
            default:
                throw new SAXPathException(Axis.lookup(axis)+" axis is unsupprted");
        }
    }

    @Override
    public void endNameStep() throws SAXPathException{}

    @Override
    public void startTextNodeStep(int axis) throws SAXPathException{
        switch(axis){
            case Axis.CHILD:
                current = new Text(current);
                break;
            default:
                throw new SAXPathException(Axis.lookup(axis)+" axis is unsupprted");
        }

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
        switch(axis){
            case Axis.DESCENDANT_OR_SELF:
                current = new DescendantSelf(current);
                break;
            default:
                throw new SAXPathException(Axis.lookup(axis)+" axis is unsupprted");
        }
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

    private boolean insidePredicate;
    @Override
    public void startPredicate() throws SAXPathException{
        insidePredicate = true;
//        throw new SAXPathException("predicate is unsupprted");
    }

    @Override
    public void endPredicate() throws SAXPathException{
        insidePredicate = false;
//        throw new SAXPathException("unsupprted");
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
        if(insidePredicate)
            current = new Position(current, number);
        else
            throw new SAXPathException("unsupprted");
    }

    @Override
    public void number(double number) throws SAXPathException{
        if(insidePredicate)
            current = new Position(current, (int)number);
        else
            throw new SAXPathException("unsupprted");
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

        XPathParser parser = new XPathParser(nsContext);

        String xpaths[] = {
            "//xsd:any/@namespace",
            "//@name",
            "/xsd:schema//xsd:complexType/@name",
            "/xsd:schema/xsd:any/@namespace",
            "/xsd:sequence/xsd:any/xs:element/@namespace",
        };

        for(String xpath: xpaths){
            Node node = parser.parse(xpath);
            System.out.println(node);
        }
    }
}

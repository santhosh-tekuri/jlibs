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

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Predicate;
import jlibs.xml.sax.sniff.model.Root;
import org.jaxen.expr.XPathExpr;
import org.jaxen.saxpath.SAXPathException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class XPath{
    private String xpath;
    private XPathExpr xpathExpr;
    List<Node> nodes = Collections.emptyList();
    List<Predicate> predicates = new ArrayList<Predicate>();

    public XPath(String xpath, XPathExpr xpathExpr, List<Node> nodes){
        this.xpath = xpath;
        this.xpathExpr = xpathExpr;
        this.nodes = nodes;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public XPath(String xpath, XPathExpr xpathExpr, List<Predicate> predicates, boolean dummy){
        this.xpath = xpath;
        this.xpathExpr = xpathExpr;
        this.predicates = predicates;
    }

    int minHits;
    public void setMinHits(int minHits){
        this.minHits = minHits;
        for(Node node: nodes)
            node.hits.setMin(minHits);
        for(Predicate predicate: predicates)
            predicate.hits.setMin(minHits);
    }

    @SuppressWarnings({"LoopStatementThatDoesntLoop"})
    public QName resultType(){
        for(Node node: nodes)
            return node.resultType();
        return XPathConstants.NODESET;
    }

    public XPath copy(Root root){
        try{
            return new JaxenParser(root).parse(xpath, xpathExpr);
        }catch(SAXPathException ex){
            throw new ImpossibleException(ex);
        }
    }

    @Override
    public String toString(){
        return xpath;
    }
}

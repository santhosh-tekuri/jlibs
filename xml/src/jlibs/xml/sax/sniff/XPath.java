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
import jlibs.xml.sax.sniff.model.NotificationListener;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.Root;
import jlibs.xml.sax.sniff.model.expr.Expression;
import jlibs.xml.sax.sniff.parser.JaxenParser;
import org.jaxen.expr.XPathExpr;
import org.jaxen.saxpath.SAXPathException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class XPath implements NotificationListener{
    private XPathExpr jaxenExpr;
    public Expression expr;

    public XPath(String xpath, XPathExpr jaxenExpr, Expression expr){
        this.jaxenExpr = jaxenExpr;
        this.expr = expr;
        this.expr.xpath = xpath;
        expr.addNotificationListener(this);
    }

    int minHits;
    public void setMinHits(int minHits){
        if(expr.resultType()!=ResultType.NODESET)
            minHits = 1;
        this.minHits = minHits;
        expr.hits.setMin(minHits);
    }

    @SuppressWarnings({"LoopStatementThatDoesntLoop"})
    public QName resultType(){
        return expr.resultType().qname();
    }

    public XPath copy(Root root){
        try{
            return new JaxenParser(root).parse(expr.xpath, jaxenExpr);
        }catch(SAXPathException ex){
            throw new ImpossibleException(ex);
        }
    }

    @Override
    public String toString(){
        return expr.xpath;
    }

    /*-------------------------------------------------[ NotificationListener ]---------------------------------------------------*/

    protected List<String> result = new ArrayList<String>();

    @Override
    @SuppressWarnings({"unchecked"})
    public void onNotification(Notifier source, Object result){
        if(result instanceof TreeMap){
            TreeMap<Integer, String> nodeSet = (TreeMap<Integer, String>)result;
            for(Map.Entry<Integer, String> nodeItem: nodeSet.entrySet())
                this.result.add(nodeItem.getValue());
        }else
            this.result.add(result.toString());
    }
}

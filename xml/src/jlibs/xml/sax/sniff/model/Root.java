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

package jlibs.xml.sax.sniff.model;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathVariableResolver;
import javax.xml.xpath.XPathFunctionResolver;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public class Root extends Node{
    public NamespaceContext nsContext;
    public XPathVariableResolver variableResolver;
    public XPathFunctionResolver functionResolver;
    public HitManager totalHits = new HitManager();

    public Root(NamespaceContext nsContext, XPathVariableResolver variableResolver, XPathFunctionResolver functionResolver){
        root = this;
        this.nsContext = nsContext;
        this.variableResolver = variableResolver;
        this.functionResolver = functionResolver;
    }

    @Override
    public boolean canBeContext(){
        return true;
    }

    @Override
    public boolean equivalent(Node node){
        return node.getClass()==getClass();
    }

    @Override
    public String toString(){
        return "Root";
    }

    /*-------------------------------------------------[ Using ]---------------------------------------------------*/

    public List<Resettable> resettables = new ArrayList<Resettable>();
    public volatile boolean using;

    public void setUsing(boolean using){
        this.using = using;
        totalHits.parsing = using;
        if(!using){
            for(Resettable resettable: resettables)
                resettable.reset();
        }
    }

    public boolean isUsing(){
        return using;
    }

    public void parsingDone(){
        totalHits.parsing = false;
    }
}

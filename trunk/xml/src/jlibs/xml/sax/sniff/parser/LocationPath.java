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

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;
import jlibs.xml.sax.sniff.model.expr.TypeCast;
import jlibs.xml.sax.sniff.model.expr.bool.Equals;
import jlibs.xml.sax.sniff.model.expr.nodeset.Count;
import jlibs.xml.sax.sniff.model.expr.nodeset.NodeSet;
import jlibs.xml.sax.sniff.model.expr.nodeset.Position;
import jlibs.xml.sax.sniff.model.expr.nodeset.Predicate;
import jlibs.xml.sax.sniff.model.expr.nodeset.event.LocalName;
import jlibs.xml.sax.sniff.model.expr.nodeset.event.NamespaceURI;
import jlibs.xml.sax.sniff.model.expr.nodeset.event.QualifiedName;
import jlibs.xml.sax.sniff.model.expr.nodeset.list.StringNodeSet;
import jlibs.xml.sax.sniff.model.expr.nodeset.list.Strings;
import jlibs.xml.sax.sniff.model.expr.nodeset.list.Sum;

import java.util.ArrayDeque;

/**
 * @author Santhosh Kumar T
 */
public class LocationPath{
    private Node contextNode;
    public ArrayDeque<StepNode> steps = new ArrayDeque<StepNode>();

    public LocationPath(Node contextNode){
        this.contextNode = contextNode;
    }

    public void addStep(Node node){
        steps.push(new StepNode(node));
    }

    public void setStep(Node node){
        steps.peek().node = node;
    }

    public void setPredicate(Expression predicate){
        LocationPath.StepNode step = steps.peek();
        if(predicate.resultType()==Datatype.NUMBER){
            Expression position = createFunction("position", step.node, step.node, step.predicate);
            step.predicate = null;
            Expression equals = new Equals(step.node);
            equals.addMember(position);
            equals.addMember(predicate);
            predicate = equals;
        }
        if(step.predicate!=null)
            predicate = new Predicate(step.node, predicate, step.predicate);
        step.predicate = predicate;
    }

    public class StepNode{
        public Node node;
        public Expression predicate;

        public StepNode(Node node){
            this.node = node;
        }
    }

    private Expression createFunction(String name, Node contextNode, Notifier member, Expression predicate){
        if("position".equals(name))
            return new Position(contextNode, member, predicate);
        else if("local-name".equals(name))
            return new LocalName(contextNode, member, predicate);
        else if("namespace-uri".equals(name))
            return new NamespaceURI(contextNode, member, predicate);
        else if("name".equals(name))
            return new QualifiedName(contextNode, member, predicate);
        else if("count".equals(name))
            return new Count(contextNode, member, predicate);
        else if("sum".equals(name))
            return new Sum(contextNode, member, predicate);
        else if(Datatype.NODESET.toString().equals(name))
            return new NodeSet(contextNode, member, predicate);
        else if("string".equals(name) || Datatype.STRING.toString().equals(name))
            return new StringNodeSet(contextNode, member, predicate);
        else if("strings".equals(name) || Datatype.STRINGS.toString().equals(name))
            return new Strings(contextNode, member, predicate);
        else if("boolean".equals(name) || Datatype.BOOLEAN.toString().equals(name))
            return new Predicate(contextNode, member, predicate);
        else if("number".equals(name) || Datatype.NUMBER.toString().equals(name)){
            TypeCast number = new TypeCast(contextNode, Datatype.NUMBER);
            number.addMember(create(Datatype.STRING));
            return number;
        }
        return null;
    }

    public Expression createFunction(String name){
        if(steps.size()==0)
            return createFunction(name, contextNode, contextNode, null);
        
        ArrayDeque<StepNode> steps = this.steps.clone();
        Notifier result = null;
        while(!steps.isEmpty()){
            StepNode step = steps.pop();
            if(step.predicate!=null){
                Notifier member = result==null ? step.node : result;
                result = createFunction(name, step.node, member, step.predicate);
            }else{
                if(result==null)
                    result = step.node;
            }
        }

        return createFunction(name, contextNode, result, null);
    }

    public Expression create(Datatype expected){
        return createFunction(expected.toString());
    }
}

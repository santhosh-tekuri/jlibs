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

package jlibs.xml.sax.sniff.model.expr.nodeset.list;

import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.engine.context.ContextListener;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.axis.Descendant;
import jlibs.xml.sax.sniff.model.expr.Expression;
import jlibs.xml.sax.sniff.model.expr.nodeset.ValidatedExpression;
import org.jaxen.saxpath.Axis;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public abstract class NodeList extends ValidatedExpression{
    public NodeList(Datatype returnType, Node contextNode, Notifier member, Expression predicate){
        super(returnType, contextNode, member, predicate);
    }

    private Node parentNode;

    @Override
    public void addMember(Notifier member){
        if(member instanceof Node){
            Node node = (Node)member;
            if(node.canBeContext()){
                node.addContextListener(new ContextListener(){
                    @Override
                    public void contextStarted(Context context, Event event){
                        for(Evaluation eval: evaluationStack){
                            NodeListEvaluation evaluation = (NodeListEvaluation)eval;
                            if(!evaluation.finished && !evaluation.resultPrepared){
                                if(canEvaluate(context.node, evaluation, context, event)){
                                    evaluation.contextStarted(context);
                                }
                            }
                        }
                    }

                    @Override
                    public void contextEnded(Context context){
                        for(Evaluation eval: evaluationStack){
                            NodeListEvaluation evaluation = (NodeListEvaluation)eval;
                            if(!evaluation.finished && !evaluation.resultPrepared){
                                evaluation.contextEnded(context);
                            }
                        }
                    }

                    @Override
                    public int priority(){
                        return evalDepth;
                    }
                });
                parentNode = node;
                node = node.addChild(new Descendant(Axis.DESCENDANT));
            }
            member = node;
        }
        super.addMember(member);
    }

    protected abstract class NodeListEvaluation extends DelayedEvaluation{
        protected Map<Object, StringBuilder> map = new HashMap<Object, StringBuilder>();
        
        @Override
        public void finish(){
            for(StringBuilder buff: map.values()){
                if(buff.length()>0)
                    consume(buff.toString());
            }
            map = null;
            super.finish();
        }

        private void consume(Event event){
            if(parentNode!=null){
                if(event.type()==Event.TEXT){
                    while(parentNode!=context.node)
                        context = context.parent;

                    StringBuilder buff = map.get(context);
                    if(buff!=null)
                        buff.append(event.getValue());
                }
            }else{
                switch(event.type()){
                    case Event.TEXT:
                    case Event.COMMENT:
                    case Event.ATTRIBUTE:
                    case Event.PI:
                        consume(event.getValue());
                        break;
                }
            }
        }

        protected abstract void consume(Object result);
        protected abstract void consume(String str);

        @Override
        @SuppressWarnings({"unchecked"})
        protected void consumeMemberResult(Object result){
            if(result instanceof Event)
                consume((Event)result);
            else
                consume(result);
        }

        public void contextStarted(Context context){
            map.put(context.identity(), new StringBuilder());
        }
        
        public void contextEnded(Context context){
            StringBuilder buff = map.remove(context);
            if(buff!=null)
                consume(buff.toString());
        }

        @Override
        protected void print(){
            debugger.println("map: %s", map);
            super.print();
        }
    }

    @Override
    public void onNotification(Notifier source, Context context, Object result){
        onNotification1(source, context, result);
    }
}

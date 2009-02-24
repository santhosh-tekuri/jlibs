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
import jlibs.xml.sax.sniff.engine.context.ContextIdentity;
import jlibs.xml.sax.sniff.engine.context.ContextListener;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.axis.Descendant;
import jlibs.xml.sax.sniff.model.expr.Expression;
import jlibs.xml.sax.sniff.model.expr.nodeset.ValidatedExpression;
import org.jaxen.saxpath.Axis;

import java.util.Iterator;
import java.util.LinkedHashMap;
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
                                    boolean started = evaluation.contextStarted(context);
                                    if(started && !event.hasChildren())
                                        evaluation.forceConsume(event);
                                }
                            }
                        }
                    }

                    @Override
                    public void contextEnded(Context context, long order){
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
        protected Map<ContextIdentity, StringBuilder> map = new LinkedHashMap<ContextIdentity, StringBuilder>();

        @Override
        public void finish(){
            for(Map.Entry<ContextIdentity, StringBuilder> entry: map.entrySet()){
                if(entry.getValue().length()>0)
                    consume(entry.getValue().toString(), entry.getKey().order);
            }
            map = null;
            super.finish();
        }

        public void forceConsume(Event event){
            switch(event.type()){
                case Event.TEXT:
                case Event.COMMENT:
                case Event.ATTRIBUTE:
                case Event.PI:
                    consume(event.getValue(), event.order());
                    break;
            }
        }

        private void consume(Event event){
            if(parentNode!=null){
                if(event.type()==Event.TEXT){
                    for(StringBuilder buff: map.values())
                        buff.append(event.getValue());
                }
            }else
                forceConsume(event);
        }

        protected abstract void consume(Object result);
        protected abstract void consume(String str, long order);

        @Override
        @SuppressWarnings({"unchecked"})
        protected void consumeMemberResult(Object result){
            if(result instanceof Event)
                consume((Event)result);
            else
                consume(result);
        }

        long lastConsumedOrder = -1;
        public boolean contextStarted(Context context){
            if(lastConsumedOrder==context.order)
                return false;
            lastConsumedOrder = context.order;
            map.put(context.identity(), new StringBuilder());
            return true;
        }
        
        @SuppressWarnings({"EqualsBetweenInconvertibleTypes"})
        public void contextEnded(Context context){
            Iterator<Map.Entry<ContextIdentity,StringBuilder>> iter = map.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<ContextIdentity, StringBuilder> entry = iter.next();
                if(context.equals(entry.getKey())){
                    iter.remove();
                    consume(entry.getValue().toString(), entry.getKey().order);
                    return;
                }
            }
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

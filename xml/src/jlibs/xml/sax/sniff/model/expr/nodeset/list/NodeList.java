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

import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.events.PI;
import jlibs.xml.sax.sniff.model.ContextListener;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.UserResults;
import jlibs.xml.sax.sniff.model.axis.Descendant;
import jlibs.xml.sax.sniff.model.expr.Expression;
import jlibs.xml.sax.sniff.model.expr.nodeset.ValidatedExpression;
import org.jaxen.saxpath.Axis;

/**
 * @author Santhosh Kumar T
 */
public abstract class NodeList extends ValidatedExpression{
    public NodeList(ResultType returnType, Node contextNode, UserResults member, Expression predicate){
        super(returnType, contextNode, member, predicate);
    }

    private boolean textOnly;

    @Override
    public void addMember(UserResults member){
        if(member instanceof Node){
            Node node = (Node)member;
            if(node.canBeContext()){
                node.addContextListener(new ContextListener(){
                    @Override
                    public void contextStarted(Event event){}

                    @Override
                    public void contextEnded(){
                        StringsEvaluation evaluation = (StringsEvaluation)evaluationStack.peek();
                        if(!evaluation.finished && !evaluation.resultPrepared)
                            evaluation.contextEnded();
                    }

                    @Override
                    public int priority(){
                        return evalDepth;
                    }
                });
                node = node.addChild(new Descendant(Axis.DESCENDANT));
                textOnly = true;

            }
            member = node;
        }
        super.addMember(member);
    }

    protected abstract class StringsEvaluation extends DelayedEvaluation{
        private StringBuilder buff = new StringBuilder();

        @Override
        public void finish(){
            if(buff.length()>0)
                consume(buff.toString());
            super.finish();
        }

        private void consume(Event event){
            String str = null;
            if(textOnly){
                if(event.type()==Event.TEXT)
                    buff.append(event.getResult());
            }else{
                switch(event.type()){
                    case Event.TEXT:
                    case Event.COMMENT:
                    case Event.ATTRIBUTE:
                        str = event.getResult();
                        break;
                    case Event.PI:
                        str = ((PI)event).data;
                        break;
                }
                consume(str);
            }
        }

        protected abstract void consume(Object result);

        @Override
        @SuppressWarnings({"unchecked"})
        protected void consumeMemberResult(Object result){
            if(result instanceof Event)
                consume((Event)result);
            else
                consume(result);
        }

        protected abstract void consume(String str);

        public void contextEnded(){
            consume(buff.toString());
            buff.setLength(0);
        }
    }
}

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

package jlibs.xml.sax.sniff.model.expr.nodeset;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.ContextListener;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Position extends ValidatedExpression{
    public Position(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.NUMBER, contextNode, member, predicate);
        contextNode.parent.addContextListener(new ContextListener(){
            @Override
            public void contextStarted(Context context, Event event){
                map.put(context.identity(), 0);
            }

            @Override
            public void contextEnded(Context context){
                map.remove(context);
            }

            @Override
            public int priority(){
                return evalDepth-1;
            }
        });
    }

    private Map<Object, Integer> map = new HashMap<Object, Integer>();

    class MyEvaluation extends DelayedEvaluation{
        @Override
        protected Object getCachedResult(){
            throw new ImpossibleException();
        }

        @Override
        protected void predicateAccepted(){
            evaluate();
        }

        private void evaluate(){
            while(contextNode.parent!=context.node)
                context = context.parent;

            if(map.get(context)==null)
                throw new ImpossibleException();

            int pos = map.get(context);
            map.put(context, ++pos);
            setResult((double)pos);
        }

        @Override
        protected void consumeMemberResult(Object result){
            if(result instanceof Event && members.size()==1) // has no predicate
                evaluate();
            else if(result instanceof Double)
                throw new ImpossibleException();
        }
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}
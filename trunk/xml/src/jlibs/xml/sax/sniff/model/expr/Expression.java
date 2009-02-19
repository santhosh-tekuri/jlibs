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

package jlibs.xml.sax.sniff.model.expr;

import jlibs.core.graph.*;
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.graph.walkers.PreorderWalker;
import jlibs.xml.sax.sniff.engine.context.Context;
import jlibs.xml.sax.sniff.engine.context.ContextListener;
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Expression extends Notifier implements ContextListener, NotificationListener{
    public HitManager hits = new HitManager();
    
    protected int evalDepth;
    protected Expression(Node contextNode, Datatype returnType, Datatype... memberTypes){
        setEvaluationStartNode(contextNode);
        setEvaluationEndNode(contextNode);
        this.returnType = returnType;
        this.memberTypes = memberTypes;
        members = new ArrayList<Notifier>(memberTypes.length);
        
        hits.totalHits = contextNode.root.totalHits;
    }

    private final Datatype returnType;

    @Override
    public Datatype resultType(){
        return returnType;
    }

    protected List<Notifier> members;
    protected final Datatype memberTypes[];

    public Datatype memberType(int index){
        return memberTypes[index];
    }
    
    public Datatype memberType(){
        return memberType(members.size());
    }

    private Notifier castTo(Notifier member, Datatype expected){
        if(member.resultType()==expected || expected==Datatype.STRINGS)
            return member;

        if(expected==Datatype.PRIMITIVE){
            switch(member.resultType()){
                case STRING:
                case BOOLEAN:
                case NUMBER:
                    return member;
                default:
                    expected = Datatype.STRING;
            }
        }
        
        TypeCast typeCast = new TypeCast(evaluationStartNode, memberType());
        typeCast.addMember(member);
        return typeCast;
    }

    public void addMember(Notifier member){
        addMember(member, memberType());
    }

    protected void addMember(Notifier member, Datatype datatype){
        member = castTo(member, datatype);
        _addMember(member);
    }

    protected final void _addMember(Notifier member){
        members.add(member);
        if(member instanceof Expression){
            Expression expr = (Expression)member;
            if(expr.evaluationEndNode.depth<evaluationEndNode.depth)
                setEvaluationEndNode(((Expression)member).evaluationEndNode);

            expr.addNotificationListener(this);
            evalDepth = Math.max(evalDepth, expr.evalDepth+1);
        }else
            member.addNotificationListener(this);
    }

    protected void printResult(String title, Object result){
        debugger.println("%s", title);
        if(result instanceof Collection){
            for(Object item: (Collection)result)
                debugger.println("      %s", item);
        }else
            debugger.println("      %s", result);
    }

    protected Object defaultValue(){
        return resultType().defaultValue();
    }

    protected List<NotificationListener> listeners = new ArrayList<NotificationListener>(3);

    @Override
    public void addNotificationListener(NotificationListener listener){
        listeners.add(listener);
    }

    @Override
    public void notify(Context context, Object result){
        for(NotificationListener listener: listeners)
            listener.onNotification(this, context, result);
    }

    public Context.ContextIdentity contextIdentityOfLastEvaluation;
    private int evaluationCount;
    public int evaluationIndex;
    protected abstract class Evaluation{
        public int id;
        public Context.ContextIdentity contextIdentity;
        public boolean finished;

        protected Evaluation(){
            id = evaluationCount++;
        }

        protected void setResult(Object result){
            evaluationIndex = id;
            if(result==null)
                result = defaultValue();

            if(debug){
                debugger.println("evaluationFinished: %s", Expression.this);
                printResult("result", result);
            }

            finished = true;
            if(result!=null){
                contextIdentityOfLastEvaluation = contextIdentity;
                Expression.this.notify(context, result);
            }
        }

        public abstract void finish();
        protected abstract void consume(Object member, Object result);
        protected abstract void print();
    }

    protected abstract Evaluation createEvaluation();

    protected Context context;

    private void consumeOnNotification(Evaluation evaluation, Notifier source, Context context, Object result){
        if(!evaluation.finished){
            if(debug){
                debugger.println("Evaluation:");
                debugger.indent++;
            }
            this.context = context;
            evaluation.consume(source, result);
            if(debug){
                if(!evaluation.finished)
                    evaluation.print();
                debugger.indent--;
            }
        }
    }
    
    @Override
    public void onNotification(Notifier source, Context context, Object result){
        if(debug){
            debugger.println("onNotification: %s", this);
            debugger.indent++;
        }

        if(source instanceof Expression){
            Expression exprSource = (Expression)source;
            if(evaluationStartNode.depth>exprSource.evaluationStartNode.depth){
                for(Evaluation evaluation: evaluationStack)
                    consumeOnNotification(evaluation, source, context, result);
            }else{
                if(exprSource.evaluationStartNode!=evaluationStartNode)
                    consumeOnNotification(evaluationStack.peek(), source, context, result);
                else{
                    for(Evaluation eval: evaluationStack){
                        if(eval.id==exprSource.evaluationIndex){
                            consumeOnNotification(eval, source, context, result);
                            break;
                        }
                    }

                }
            }
        }else
            consumeOnNotification(evaluationStack.peek(), source, context, result);

        if(debug)
            debugger.indent--;
    }

    protected void evalutate(Evaluation evaluation, Notifier source, Context context, Object result){
        if(!evaluation.finished){
            if(debug){
                debugger.println("Evaluation:");
                debugger.indent++;
            }
            this.context = context;
            evaluation.consume(source, result);
            if(debug){
                if(!evaluation.finished)
                    evaluation.print();
                debugger.indent--;
            }
        }
    }

/*-------------------------------------------------[ Context ]---------------------------------------------------*/

    public Node evaluationStartNode, evaluationEndNode;

    public void setEvaluationStartNode(Node node){
        if(evaluationStartNode==node)
            return;
        
        if(evaluationStartNode!=null)
            evaluationStartNode.removeContextStartListener(this);

        evaluationStartNode = node;
        depth = evaluationStartNode.depth;
        evaluationStartNode.addContextStartListener(this);
    }

    public void setEvaluationEndNode(Node node){
        if(evaluationEndNode==node)
            return;
        
        if(evaluationEndNode!=null)
            evaluationEndNode.removeContextEndListener(this);

        evaluationEndNode = node;
        evaluationEndNode.addContextEndListener(this);
    }

    protected ArrayDeque<Evaluation> evaluationStack = new ArrayDeque<Evaluation>();

    @Override
    public void contextStarted(Context context, Event event){
        if(debug)
            debugger.println("newEvaluation: %s", this);
        Expression.Evaluation evaluation = createEvaluation();
        evaluation.contextIdentity = context.identity();
        evaluationStack.push(evaluation);
    }

    @Override
    public void contextEnded(Context context){
        if(evaluationStartNode==evaluationEndNode){
            Evaluation eval = evaluationStack.pop();
            if(!eval.finished){
                this.context = context; 
                eval.finish();
                if(debug)
                    debugger.println("finishedEvaluation: %s", this);
            }
        }else{
            while(!evaluationStack.isEmpty()){
                Evaluation eval = evaluationStack.pop();
                if(!eval.finished){
                    eval.finish();
                    if(debug)
                        debugger.println("finishedEvaluation: %s", this);
                }
            }
        }
    }

    @Override
    public int priority(){
        return evalDepth;
    }

    public String xpath;

    /*-------------------------------------------------[ ToString ]---------------------------------------------------*/

    public String getName(){
        String name = getClass().getSimpleName();
        if(name.endsWith("Expression"))
            name = name.substring(0, name.length()-"Expression".length());
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    @Override
    public final String toString(){
        StringBuilder buff = new StringBuilder();
        if(xpath!=null)
            buff.append("UserGiven");
        for(Object member: members){
            if(buff.length()>0)
                buff.append(", ");
            buff.append(member);
        }
        return getName()+'_'+depth+'('+buff+')';
    }

    public void reset(){
        hits.reset();
        evaluationStack.clear();
        context = null;
        contextIdentityOfLastEvaluation = null;
        for(Notifier member: members){
            if(member instanceof Expression)
                ((Expression)member).reset();
        }
    }

    /*-------------------------------------------------[ Debug ]---------------------------------------------------*/

    public void print(){
        Navigator<Notifier> navigator = new Navigator<Notifier>(){
            @Override
            public Sequence<? extends Notifier> children(Notifier elem){
                if(elem instanceof Expression)
                    return new IterableSequence<Notifier>(((Expression)elem).members);
                else
                    return EmptySequence.getInstance();
            }
        };
        Walker<Notifier> walker = new PreorderWalker<Notifier>(this, navigator);
        WalkerUtil.print(walker, new Visitor<Notifier, String>(){
            @Override
            public String visit(Notifier elem){
                if(elem instanceof Expression){
                    Expression expr = (Expression)elem;
                    return String.format("%s_%d_%d {%s_%d, %s_%d}", expr.getName(), expr.depth, expr.evalDepth, expr.evaluationStartNode.toString(), expr.evaluationStartNode.depth, expr.evaluationEndNode.toString(), expr.evaluationEndNode.depth);
                }else{
                    Node node = (Node)elem;
                    return String.format("%s_%d", node, node.depth);
                }
            }
        });
    }
}

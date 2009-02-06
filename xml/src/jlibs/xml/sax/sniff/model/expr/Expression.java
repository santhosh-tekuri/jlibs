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
import jlibs.xml.sax.sniff.events.Event;
import jlibs.xml.sax.sniff.model.ContextListener;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.UserResults;

import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public abstract class Expression extends Node implements ContextListener, jlibs.xml.sax.sniff.model.EventListener, ResultListener{
    protected int evalDepth;
    protected Expression(Node contextNode, ResultType returnType, ResultType... memberTypes){
        this.contextNode = contextNode;
        this.returnType = returnType;
        this.memberTypes = memberTypes;
        
        depth = contextNode.depth;
        hits.totalHits = contextNode.hits.totalHits;
        contextNode.addContextListener(this);
    }

    private ResultType returnType;
    @Override
    public ResultType resultType(){
        return returnType;
    }

    public boolean equivalent(Node node){
        return false;
    }

    protected List<UserResults> members = new ArrayList<UserResults>();
    protected final ResultType memberTypes[];

    public ResultType memberType(int index){
        return memberTypes[index];
    }
    
    public ResultType memberType(){
        return memberTypes[members.size()];
    }

    private UserResults castTo(UserResults member, ResultType expected){
        if(member.resultType()==expected || expected==ResultType.STRINGS)
            return member;

        TypeCast typeCast = new TypeCast(contextNode, memberType());
        typeCast.addMember(member);
        return member;
    }

    public void addMember(UserResults member){
        addMember(member, memberTypes[members.size()]);
    }

    protected void addMember(UserResults member, ResultType resultType){
        member = castTo(member, resultType);
        _addMember(member);
    }

    protected final void _addMember(UserResults member){
        if(member.depth<depth)
            throw new IllegalArgumentException();
        
        members.add(member);
        if(member instanceof Expression){
            Expression expr = (Expression) member;
            expr.listener = this;
            evalDepth = Math.max(evalDepth, expr.evalDepth+1);
        }else
            ((Node)member).addEventListener(this);
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

    public ResultListener listener;
    protected abstract class Evaluation{
        public boolean finished;

        protected final void setResult(Object result){
            if(result==null)
                result = defaultValue();

            if(debug){
                debugger.println("evaluationFinished: %s", Expression.this);
                printResult("result", result);
            }
            
            finished = true;
            if(result!=null)
                listener.finishedEvaluation(Expression.this, result);
        }

        public abstract void finish();
        protected abstract void consume(Object member, Object result);
        protected abstract void print();
    }

    protected abstract Evaluation createEvaluation();

    @Override
    public void onEvent(Node node, Event event){
        if(debug){
            debugger.println("onEvent: %s", this);
            debugger.indent++;
        }

        Evaluation evaluation = evaluationStack.peek();
        if(!evaluation.finished){
            if(debug){
                debugger.println("Evaluation:");
                debugger.indent++;
            }
            evaluation.consume(node, event);
            if(debug){
                if(!evaluation.finished)
                    evaluation.print();
                debugger.indent--;
            }
        }

        if(debug)
            debugger.indent--;
    }

    @Override
    public void finishedEvaluation(Expression member, Object result){
        if(debug){
            debugger.println("onMemberFinish: %s", this);
            debugger.indent++;
        }

        Evaluation evaluation = evaluationStack.peek();
        if(!evaluation.finished){
            if(debug){
                debugger.println("Evaluation:");
                debugger.indent++;
            }
            evaluation.consume(member, result);
            if(debug){
                if(!evaluation.finished)
                    evaluation.print();
                debugger.indent--;
            }
        }

        if(debug)
            debugger.indent--;
    }

/*-------------------------------------------------[ Context ]---------------------------------------------------*/

    protected final Node contextNode;
    protected ArrayDeque<Evaluation> evaluationStack = new ArrayDeque<Evaluation>();
    
    @Override
    public void contextStarted(Event event){
        if(debug)
            debugger.println("newEvaluation: %s", this);
        evaluationStack.push(createEvaluation());
    }

    @Override
    public void contextEnded(){
        Evaluation eval = evaluationStack.pop();
        if(!eval.finished)
            eval.finish();
        if(debug)
            debugger.println("finishedEvaluation: %s", this);
    }

    @Override
    public int priority(){
        return evalDepth;
    }

    @Override
    public void userGiven(String xpath){
        super.userGiven(xpath);
        listener = new ResultListener(){
            @Override
            @SuppressWarnings({"unchecked"})
            public void finishedEvaluation(Expression expr, Object result){
                if(result instanceof TreeMap){
                    TreeMap<Integer, String> nodeSet = (TreeMap<Integer, String>)result;
                    for(Map.Entry<Integer, String> nodeItem: nodeSet.entrySet())
                        addResult(nodeItem.getKey(), nodeItem.getValue());
                }else
                    addResult(-1, result.toString());
            }
        };
    }

    public void destroy(){
        contextNode.removeContextListener(this);
        for(Object member: members){
            if(member instanceof Expression)
                ((Expression)member).listener = null;
            else if(member instanceof Node)
                ((Node)member).removeEventListener(this);
        }
    }

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
        if(userGiven)
            buff.append("UserGiven");
        for(Object member: members){
            if(buff.length()>0)
                buff.append(", ");
            buff.append(member);
        }
        return getName()+'_'+depth+'('+buff+')';
    }

    /*-------------------------------------------------[ Debug ]---------------------------------------------------*/

    public void print(){
        Navigator<UserResults> navigator = new Navigator<UserResults>(){
            @Override
            public Sequence<? extends UserResults> children(UserResults elem){
                if(elem instanceof Expression)
                    return new IterableSequence<UserResults>(((Expression)elem).members);
                else
                    return EmptySequence.getInstance();
            }
        };
        Walker<UserResults> walker = new PreorderWalker<UserResults>(this, navigator);
        WalkerUtil.print(walker, new Visitor<UserResults, String>(){
            @Override
            public String visit(UserResults elem){
                if(elem instanceof Expression){
                    Expression expr = (Expression)elem;
                    return String.format("%s_%d_%d @@@ %s_%d", expr.getName(), expr.depth, expr.evalDepth, expr.contextNode.toString(), expr.contextNode.depth);
                }else{
                    Node node = (Node)elem;
                    return String.format("%s_%d", node, node.depth);
                }
            }
        });
    }
}

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

import jlibs.core.graph.Sequence;
import jlibs.core.graph.sequences.DuplicateSequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.bool.Comparison;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class Function extends Expression{
    protected Function(Node contextNode, Datatype returnType, Datatype... memberTypes){
        super(contextNode, returnType, memberTypes);
    }

    final class MyEvaluation extends Evaluation{
        private int pending = members.size();
        private Object results[] = new Object[pending];

        @Override
        public void finish(){
            throw new ImpossibleException();
        }

        @Override
        protected void consume(Object member, Object result){
            int i = 0;
            for(Notifier _member: members){
                if(_member==member){
                    results[i] = result;
                    pending--;
                    if(pending==0){
                        setResult(evaluate(results));
                        return;
                    }else{
                        Object r = evaluatePending(results);
                        if(r!=null){
                            setResult(r);
                            return;
                        }
                    }
                }
                i++;
            }
        }

        @SuppressWarnings({"unchecked"})
        private Object evaluate(Object args[]){
            boolean multiResults = false;
            if(!(Function.this instanceof Comparison)){
                for(int i=0; i<args.length; i++){
                    if(args[i] instanceof List && members.get(i).resultType()!=Datatype.STRINGS){
                        multiResults = true;
                        break;
                    }
                }
            }
            if(multiResults){
                List results = new ArrayList();
                Sequence seq[] = new Sequence[args.length];
                for(int i=0; i<args.length; i++){
                    if(args[i] instanceof List && members.get(i).resultType()!=Datatype.STRINGS)
                        seq[i] = new IterableSequence((List)args[i]);
                    else
                        seq[i] = new DuplicateSequence(args[i]);
                }

                for(int i=0; i<seq.length; i++)
                    args[i] = seq[i].next();
                results.add(Function.this.evaluate(args));

                while(true){
                    int i=seq.length-1;
                    while(true){
                        if(seq[i].hasNext()){
                            args[i] = seq[i].next();
                            while(++i<seq.length)
                                args[i] = seq[i].next();
                            results.add(Function.this.evaluate(args));
                            break;
                        }else{
                            if(i==0)
                                return results;
                            seq[i].reset();
                            i--;
                        }
                    }
                }
            }else
                return Function.this.evaluate(args);
        }

        @Override
        protected void print(){}
    }

    protected abstract Object evaluate(Object args[]);
    protected Object evaluatePending(Object args[]){
        return null;
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}

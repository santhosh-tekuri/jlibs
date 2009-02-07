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
import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;

/**
 * @author Santhosh Kumar T
 */
public class StringNodeSet extends NodeList{
    public StringNodeSet(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.STRING, contextNode, member, predicate);
    }

    class MyEvaluation extends StringsEvaluation{
        private String str;

        @Override
        protected void consume(Object result){
            str = (String)result;
            resultPrepared();
        }

        @Override
        protected void consume(String str){
            this.str = str;
            resultPrepared();
        }

        @Override
        protected Object getCachedResult(){
            return str;
        }

        @Override
        public void contextStarted(Context context){
            if(map.size()==0)
                super.contextStarted(context);
        }
    }
    
    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}

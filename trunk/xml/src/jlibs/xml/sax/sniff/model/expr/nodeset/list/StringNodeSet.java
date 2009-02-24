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

import java.util.TreeMap;

/**
 * @author Santhosh Kumar T
 */
public class StringNodeSet extends NodeList{
    public StringNodeSet(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.STRING, contextNode, member, predicate);
    }

    class MyEvaluation extends NodeListEvaluation{
        private long order;
        private String str;

        @Override
        protected void consume(Object result){
            long _order = ((Expression)members.get(0)).contextIdentityOfLastEvaluation.order;
            if(str!=null){
                if(_order>order)
                    return;
            }
            order = _order;
            this.str = (String)result;
        }

        @Override
        protected void consume(String str, long order){
            this.str = str;
            this.order = order;
            resultPrepared();
        }

        @Override
        protected Object getCachedResult(){
            if(storeDocumentOrder){
                TreeMap<Long, String> map = new TreeMap<Long, String>();
                map.put(order, str);
                return map;
            }else
                return str;
        }

        @Override
        @SuppressWarnings({"SimplifiableIfStatement"})
        public boolean contextStarted(Context context){
            if(map.size()==0)
                return super.contextStarted(context);
            else
                return false;
        }
    }
    
    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}

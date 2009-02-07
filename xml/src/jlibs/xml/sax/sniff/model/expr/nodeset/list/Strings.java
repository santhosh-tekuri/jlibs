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

import jlibs.xml.sax.sniff.model.Datatype;
import jlibs.xml.sax.sniff.model.Node;
import jlibs.xml.sax.sniff.model.Notifier;
import jlibs.xml.sax.sniff.model.expr.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Strings extends NodeList{
    public Strings(Node contextNode, Notifier member, Expression predicate){
        super(Datatype.STRINGS, contextNode, member, predicate);
    }

    class MyEvaluation extends StringsEvaluation{
        private List<String> strings = new ArrayList<String>();

        @Override
        @SuppressWarnings({"unchecked"})
        protected void consume(Object result){
            strings.addAll((List<String>)result);
        }

        @Override
        protected void consume(String str){
            strings.add(str);
        }

        @Override
        protected Object getCachedResult(){
            return strings;
        }
    }

    @Override
    protected Evaluation createEvaluation(){
        return new MyEvaluation();
    }
}
/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.examples.xml.sax.dog;

import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.expr.Evaluation;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.InstantEvaluationListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
@SuppressWarnings({"unchecked"})
public class InstantXPathResults extends InstantEvaluationListener{
    final Map<Expression, Object> results = new HashMap<Expression, Object>();
    final Object instantResults[];

    public InstantXPathResults(int documentXPathsCount){
        instantResults = new Object[documentXPathsCount];
    }

    @Override
    public void onNodeHit(Expression expression, NodeItem nodeItem){
        LongTreeMap<NodeItem> map = (LongTreeMap<NodeItem>)instantResults[expression.id];
        if(map==null)
            instantResults[expression.id] = map = new LongTreeMap<NodeItem>();
        map.put(nodeItem.order, nodeItem);
    }

    @Override
    public void finished(Evaluation evaluation){
        Object result = evaluation.getResult();
        if(result==null){
            if(instantResults[evaluation.expression.id]==null)
                results.put(evaluation.expression, Collections.<Object>emptyList());
            else{
                LongTreeMap<NodeItem> map = (LongTreeMap<NodeItem>)instantResults[evaluation.expression.id];
                results.put(evaluation.expression, new ArrayList(map.values()));
            }
        }else
            results.put(evaluation.expression, result);
    }

    public Object getResult(Expression expr){
        return results.get(expr);
    }
}

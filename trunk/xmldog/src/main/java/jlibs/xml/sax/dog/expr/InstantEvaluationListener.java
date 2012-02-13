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

package jlibs.xml.sax.dog.expr;

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.expr.nodset.PathExpression;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public abstract class InstantEvaluationListener extends EvaluationListener{
    public abstract void onNodeHit(Expression expression, NodeItem nodeItem);

    public abstract void finishedNodeSet(Expression expression);
    public abstract void onResult(Expression expression, Object result);

    @Override
    @SuppressWarnings("unchecked")
    public final void finished(Evaluation evaluation){
        Expression expression = evaluation.expression;
        if(expression.getXPath()==null)
            return;

        Object result = evaluation.getResult();
        boolean isNodeSet;
        if(expression.resultType==DataType.NODESET){
            if(expression instanceof PathExpression)
                isNodeSet = !((PathExpression)expression).forEach;
            else
                isNodeSet = true;
        }else
            isNodeSet = false;

        if(result==null){
            finishedNodeSet(expression);
        }else{
            if(isNodeSet){
                for(NodeItem nodeItem: (List<NodeItem>)result)
                    onNodeHit(expression, nodeItem);
                finishedNodeSet(expression);
            }else
                onResult(expression, result);
        }
    }
}

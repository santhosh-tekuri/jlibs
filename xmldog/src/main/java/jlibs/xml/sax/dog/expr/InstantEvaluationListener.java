/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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

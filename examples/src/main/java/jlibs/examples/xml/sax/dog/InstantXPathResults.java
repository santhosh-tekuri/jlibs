/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

package jlibs.examples.xml.sax.dog;

import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.NodeItem;
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
    public void finishedNodeSet(Expression expression){
        LongTreeMap<NodeItem> map = (LongTreeMap<NodeItem>)instantResults[expression.id];
        if(map==null)
            results.put(expression, Collections.<Object>emptyList());
        else
            results.put(expression, new ArrayList(map.values()));
    }

    @Override
    public void onResult(Expression expression, Object result){
        results.put(expression, result);
    }

    public Object getResult(Expression expr){
        return results.get(expr);
    }
}

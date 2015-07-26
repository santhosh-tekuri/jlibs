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

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class Count extends LocationExpression{
    public Count(LocationPath locationPath){
        super(locationPath, DataType.NUMBER, true, true);
    }

    @Override
    public Object getResult(){
        return locationPath==LocationPath.IMPOSSIBLE ? DataType.ZERO : DataType.ONE;
    }

    @Override
    protected Object getResultItem(Event event){
        return DataType.ONE;
    }

    @Override
    @SuppressWarnings({"UnnecessaryBoxing"})
    protected Object getResult(LongTreeMap<Object> result){
        if(rawResult)
            return result;
        else
            return new Double(result.size());
    }

    @Override
    protected String getName(){
        return "count";
    }

    @Override
    public Expression simplify(){
        if(locationPath.scope==Scope.LOCAL && locationPath.steps.length==0) // count(.) is always 1
            return new Literal(DataType.ONE, DataType.NUMBER);
        else
            return super.simplify();
    }
}
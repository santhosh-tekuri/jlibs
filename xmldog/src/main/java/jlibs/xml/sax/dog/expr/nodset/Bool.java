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

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class Bool extends LocationExpression{
    public Bool(LocationPath locationPath){
        super(locationPath.scope, locationPath, DataType.BOOLEAN, false, false);
    }

    @Override
    public Object getResult(){
        return locationPath==LocationPath.IMPOSSIBLE ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    protected Object getResultItem(Event event){
        return Boolean.TRUE;
    }

    @Override
    protected String getName(){
        return "boolean";
    }

    @Override
    public Expression simplify(){
        if(locationPath.scope==Scope.LOCAL && locationPath.steps.length==0)
            return new Literal(Boolean.TRUE, DataType.BOOLEAN);
        else
            return super.simplify();
    }
}
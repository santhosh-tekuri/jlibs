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

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.path.LocationPath;

/**
 * @author Santhosh Kumar T
 */
public abstract class FirstEventData extends LocationExpression{
    public FirstEventData(LocationPath locationPath){
        super(locationPath.scope, locationPath, DataType.STRING, false, true);
    }

    @Override
    public final Object getResult(){
        return "";
    }

    @Override
    public Expression simplify(){
        Expression expr = super.simplify();
        if(expr!=this)
            return expr;

        if(locationPath.steps.length>0){
            // for text() and comment(), namespace-uri(), local-name(), name() is empty
            int id = locationPath.steps[locationPath.steps.length-1].constraint.id;
            if(id==Constraint.ID_TEXT || id==Constraint.ID_COMMENT)
                return new Literal("", DataType.STRING);
        }
        return this;
    }
}
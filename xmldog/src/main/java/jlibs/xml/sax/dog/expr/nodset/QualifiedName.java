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
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class QualifiedName extends FirstEventData{
    public QualifiedName(LocationPath locationPath){
        super(locationPath);
    }

    @Override
    protected Object getResultItem(Event event){
        return event.qualifiedName();
    }

    @Override
    protected String getName(){
        return "name";
    }

    @Override
    public Expression simplify(){
        if(locationPath.steps.length>0){
            // for ::text(), name() is empty
            if(locationPath.steps[locationPath.steps.length-1].constraint.id==Constraint.ID_TEXT)
                return new Literal("", DataType.STRING);
        }
        return super.simplify();
    }
}
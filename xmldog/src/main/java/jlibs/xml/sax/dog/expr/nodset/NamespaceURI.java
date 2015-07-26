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
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
import jlibs.xml.sax.dog.path.Axis;
import jlibs.xml.sax.dog.path.Constraint;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.path.Step;
import jlibs.xml.sax.dog.path.tests.PITarget;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class NamespaceURI extends FirstEventData{
    public NamespaceURI(LocationPath locationPath){
        super(locationPath);
    }

    @Override
    protected Object getResultItem(Event event){
        return event.namespaceURI();
    }

    @Override
    protected String getName(){
        return "namespace-uri";
    }

    @Override
    public Expression simplify(){
        if(locationPath.steps.length>0){
            Step lastStep = locationPath.steps[locationPath.steps.length-1];
            // for ::text(), ::processing-instruction() and namespace::, namespace-uri() is empty
            int id = lastStep.constraint.id;
            if(id==Constraint.ID_TEXT || id==Constraint.ID_PI || lastStep.constraint instanceof PITarget
                    || lastStep.axis==Axis.NAMESPACE)
                return new Literal("", DataType.STRING);
        }
        return super.simplify();
    }
}
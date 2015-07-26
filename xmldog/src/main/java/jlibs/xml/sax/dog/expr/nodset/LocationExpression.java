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
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.path.LocationPath;
import jlibs.xml.sax.dog.sniff.Event;

import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public abstract class LocationExpression extends Expression{
    public boolean rawResult;
    public final LocationPath locationPath;
    public final boolean many;
    public final boolean first;

    public LocationExpression(LocationPath locationPath, DataType resultType, boolean many, boolean first){
        this(locationPath.scope, locationPath, resultType, many, first);
    }

    public LocationExpression(int scope, LocationPath locationPath, DataType resultType, boolean many, boolean first){
        super(scope, resultType);
        this.locationPath = locationPath;
        this.many = many;
        this.first = first;
    }

    @Override
    public Object getResult(Event event){
        if(locationPath.steps.length==0)
            return getResultItem(event);
        else
            return new LocationEvaluation(this, 0, event, event.getID());
    }

    protected abstract Object getResultItem(Event event);

    protected Object getResult(LongTreeMap<Object> result){
        if(rawResult)
            return result;
        
        switch(resultType){
            case NODESET:
            case STRINGS:
            case NUMBERS:
                if(xpath==null)
                    return result.values();
                else
                    return new ArrayList<Object>(result.values());
            case NUMBER:
                double d = 0;
                for(LongTreeMap.Entry entry=result.firstEntry(); entry!=null; entry=entry.next())
                    d += (Double)entry.value;
                return d;
            case BOOLEAN:
                return !result.isEmpty();
            default:
                if(result.isEmpty())
                    return resultType.defaultValue;
                else
                    return result.firstEntry().value;
        }
    }

    protected abstract String getName();

    @Override
    public final String toString(){
        return String.format("%s(%s)", getName(), locationPath);
    }
}

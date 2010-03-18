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

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.expr.Expression;
import jlibs.xml.sax.dog.expr.Literal;
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
    public final Object getResult(Event event){
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

    @Override
    public Expression simplify(){
        if(locationPath.scope==Scope.DOCUMENT && locationPath.steps.length==0)
            return new Literal(getResult(), resultType);
        return super.simplify();
    }

    protected abstract String getName();

    @Override
    public final String toString(){
        return String.format("%s(%s)", getName(), locationPath);
    }
}

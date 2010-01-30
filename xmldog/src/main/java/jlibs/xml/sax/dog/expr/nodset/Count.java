/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
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
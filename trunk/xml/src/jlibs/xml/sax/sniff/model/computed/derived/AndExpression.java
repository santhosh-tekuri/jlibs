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

package jlibs.xml.sax.sniff.model.computed.derived;

import jlibs.xml.sax.sniff.model.ResultType;

/**
 * @author Santhosh Kumar T
 */
public class AndExpression extends DerivedResults{
    public AndExpression(){
        super(ResultType.BOOLEAN, false, ResultType.BOOLEAN, ResultType.BOOLEAN);
    }

    @Override
    protected String deriveResult(String[] memberResults){
        return String.valueOf(Boolean.valueOf(memberResults[0]) && Boolean.valueOf(memberResults[1]));
    }
}

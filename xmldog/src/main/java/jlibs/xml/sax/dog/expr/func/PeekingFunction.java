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

package jlibs.xml.sax.dog.expr.func;

import jlibs.xml.sax.dog.DataType;

/**
 * PeekingFunction is a function which can evaluate its results before all its arguments are known.
 * Note that such prediction might be possible only in some cases.
 * For example:
 *      Any arthimetic operation whose one operand is infinity always returns infinity
 *
 * @author Santhosh Kumar T
 */
public abstract class PeekingFunction extends Function{
    protected PeekingFunction(String name, DataType resultType, boolean varArgs, DataType... memberTypes){
        super(name, resultType, varArgs, memberTypes);
    }

    protected PeekingFunction(String name, DataType resultType, boolean varArgs, int mandatory, DataType... memberTypes){
        super(name, resultType, varArgs, mandatory, memberTypes);
    }

    /**
     * After each member result is evaluated, this method is called.
     * Returns result(non-null), if the function can be evaluated.
     */
    protected abstract Object onMemberResult(int index, Object result);
}

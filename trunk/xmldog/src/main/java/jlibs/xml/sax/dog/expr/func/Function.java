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

package jlibs.xml.sax.dog.expr.func;

import jlibs.core.lang.ArrayUtil;
import jlibs.xml.sax.dog.DataType;

/**
 * @author Santhosh Kumar T
 */
public abstract class Function{
    public final String name;
    public final DataType resultType;
    public final DataType memberTypes[];
    public final boolean varArgs;

    /**
     * tells how many arguments from beginning are mandatory.
     * in case varArgs is :
     *      o false: varArgs==membterTypes.lenth
     *      o true: varArgs==memberTypes.length or varArgs==memberTypes.length-1 
     */
    public final int mandatory;

    protected Function(String name, DataType resultType, boolean varArgs, DataType... memberTypes){
        this(name, resultType, varArgs, memberTypes.length, memberTypes);
    }

    protected Function(String name, DataType resultType, boolean varArgs, int mandatory, DataType... memberTypes){
        this.name = name;
        this.resultType = resultType;
        this.memberTypes = memberTypes;
        this.varArgs = varArgs;
        this.mandatory = mandatory;
    }

    public final boolean canAccept(int noOfMembers){
        return noOfMembers>=mandatory && (varArgs || noOfMembers<=memberTypes.length);
    }

    public final DataType memberType(int i){
        return i<memberTypes.length ? memberTypes[i] : memberTypes[memberTypes.length-1];
    }

    public abstract Object evaluate(Object... args);

    private static final String operators[] = { "+", "-", "*", "div", "mod", "and", "or", "|", "=", "!=", ">", ">=", "<", "<=" };
    public final boolean isOperator(){
        return ArrayUtil.indexOf(operators, name)!=-1;
    }
}

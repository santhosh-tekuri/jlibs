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

package jlibs.xml.sax.dog.path;

/**
 * @author Santhosh Kumar T
 */
public class Step extends Predicated{
    public final int axis;
    public final Constraint constraint;

    public Step(int axis, Constraint constraint){
        this.axis = axis;
        this.constraint = constraint;
    }

    @Override
    public String toString(){
        if(predicateSet.predicate==null)
            return String.format("%s::%s", Axis.names[axis], constraint);
        else{
            StringBuilder buff = new StringBuilder();
            for(PositionalPredicate positionPredicate=predicateSet.headPositionalPredicate; positionPredicate!=null; positionPredicate=positionPredicate.next)
                buff.append('[').append(positionPredicate.predicate).append(']');
            return String.format("%s::%s%s[%s]", Axis.names[axis], constraint, buff, predicateSet.predicate);
        }
    }
}

/*
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

package jlibs.xml.sax.binding;

/**
 * Encapsulates both binding and relation. This is used by BindingRegistry.
 *
 * @author Santhosh Kumar T
 */
public class BindingRelation<P, C>{
    Binding<C> binding;
    Relation<P, C> relation;

    BindingRelation(Binding<C> binding, Relation<P, C> relation){
        this.binding = binding;
        this.relation = relation;
    }

    @SuppressWarnings({"unchecked"})
    public static final BindingRelation<?, ?> DO_NOTHING = new BindingRelation(Binding.doNothing(), Relation.doNothing());
}

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

package jlibs.xml.sax.binding.impl;

import javax.xml.namespace.QName;

/**
 * Encapsulates both binding and relation. This is used by Registry.
 *
 * @author Santhosh Kumar T
 */
public class BindingRelation{
    public QName qname;

    public int bindingState;
    public Binding binding;

    public int relationState;
    public Relation relation;

    public BindingRelation(QName qname, int bindingState, Binding binding, int relationState, Relation relation){
        this.qname = qname;
        this.bindingState = bindingState;
        this.binding = binding;
        this.relationState = relationState;
        this.relation = relation;
    }

    public static final BindingRelation DO_NOTHING = new BindingRelation(Registry.ANY, -1, Binding.DO_NOTHING, -1, Relation.DO_NOTHING);
}

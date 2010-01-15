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

import jlibs.xml.sax.binding.SAXContext;

/**
 * @author Santhosh Kumar T
 */
public final class TextBinding extends Binding{
    public static final TextBinding INSTANCE = new TextBinding();
    private TextBinding(){}

    @Override
    public void text(int state, SAXContext current, String text){
        current.object = text;
    }
}

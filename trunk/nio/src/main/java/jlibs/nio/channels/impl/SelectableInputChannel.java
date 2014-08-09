/*
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

package jlibs.nio.channels.impl;

import jlibs.nio.channels.InputChannel;
import jlibs.nio.channels.impl.filters.InputFilterChannel;

import java.nio.channels.SelectionKey;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface SelectableInputChannel extends SelectableChannel, InputChannel{
    public void setAppInput(InputFilterChannel input);
    public InputFilterChannel getAppInput();
    public default void addReadInterest(){
        if(isOpen())
            addInterestOps(SelectionKey.OP_READ);
    }
}

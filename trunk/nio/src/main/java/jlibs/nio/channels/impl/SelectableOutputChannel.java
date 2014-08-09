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

import jlibs.nio.channels.OutputChannel;
import jlibs.nio.channels.impl.filters.OutputFilterChannel;

import java.nio.channels.SelectionKey;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface SelectableOutputChannel extends SelectableChannel, OutputChannel{
    public void setAppOutput(OutputFilterChannel output);
    public OutputFilterChannel getAppOutput();
    public default void addWriteInterest(){
        if(isOpen())
            addInterestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public default boolean isFlushed(){
        return true;
    }
}
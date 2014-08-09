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

package jlibs.nio.channels.impl.filters;

import jlibs.nio.channels.impl.SelectableOutputChannel;

/**
 * @author Santhosh Kumar Tekuri
 */
public interface OutputFilterChannel extends FilterChannel, SelectableOutputChannel{
    public void setPeerOutput(SelectableOutputChannel output);
    public SelectableOutputChannel getPeerOutput();

    @Override
    public default boolean isFlushed(){
        return selfInterestOps()==0 && getPeerOutput().isFlushed();
    }

    @Override
    public default boolean isClosed(){
        return !isOpen() && (getPeerOutput()==null || isFlushed());
    }
}

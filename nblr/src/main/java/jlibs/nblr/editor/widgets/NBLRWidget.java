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

package jlibs.nblr.editor.widgets;

import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public interface NBLRWidget{
    public static Color COLOR_HILIGHT = new Color(0, 128, 0);
    public static Color COLOR_DEBUGGER = Color.RED;

    public void highLight(boolean doHighLight);
    public void executing(boolean executing);
}

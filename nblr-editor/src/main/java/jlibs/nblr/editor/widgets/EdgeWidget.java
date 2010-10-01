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

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class EdgeWidget extends ConnectionWidget implements NBLRWidget{
    private static Color COLOR = new Color(0, 0, 128);

    public EdgeWidget(Scene scene){
        super(scene);
    }

    @Override
    public void highLight(boolean doHighLight){
        Color color;
        if(doHighLight)
            color = COLOR_HILIGHT;
        else
            color = executing ? COLOR_DEBUGGER : COLOR;

        setLineColor(color);
        getChildren().get(0).setForeground(color);
    }

    private boolean executing = false;
    @Override
    public void executing(boolean executing){
        this.executing = executing;
        highLight(false);
    }
}

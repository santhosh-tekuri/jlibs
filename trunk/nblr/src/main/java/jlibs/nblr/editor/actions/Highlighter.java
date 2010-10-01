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

package jlibs.nblr.editor.actions;

import jlibs.nblr.editor.widgets.NBLRWidget;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author Santhosh Kumar T
 */
public class Highlighter implements TwoStateHoverProvider{
    private TwoStateHoverProvider delegate;
    public Highlighter(TwoStateHoverProvider delegate){
        this.delegate = delegate;
    }

    private Widget resolve(Widget widget){
        ObjectScene scene = (ObjectScene)widget.getScene();
        return scene.findWidget(scene.findObject(widget));
    }

    public void unsetHovering(Widget widget){
        widget = resolve(widget);
        if(widget==null)
            return;
        ((NBLRWidget)widget).highLight(false);
        if(delegate!=null)
            delegate.unsetHovering(widget);
    }

    public void setHovering(Widget widget){
        widget = resolve(widget);
        if(widget==null)
            return;
        ((NBLRWidget)widget).highLight(true);

        if(delegate!=null)
            delegate.setHovering(widget);
    }
}

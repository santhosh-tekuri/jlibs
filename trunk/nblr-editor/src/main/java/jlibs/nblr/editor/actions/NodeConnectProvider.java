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

import jlibs.nblr.editor.RuleScene;
import jlibs.nblr.rules.Node;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

import static jlibs.nblr.editor.Util.model;
import static jlibs.nblr.editor.Util.node;

/**
 * @author Santhosh Kumar T
 */
public class NodeConnectProvider implements ConnectProvider{
    public boolean isSourceWidget(Widget sourceWidget){
        return model(sourceWidget) instanceof Node;
    }

    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget){
        return model(targetWidget) instanceof Node ? ConnectorState.ACCEPT : ConnectorState.REJECT;
    }

    public boolean hasCustomTargetWidgetResolver(Scene scene){
        return false;
    }

    public Widget resolveTargetWidget (Scene scene, Point sceneLocation){
        return null;
    }

    public void createConnection(Widget sourceWidget, Widget targetWidget){
        node(sourceWidget).addEdgeTo(node(targetWidget));
        ((RuleScene)sourceWidget.getScene()).refresh();
    }
}

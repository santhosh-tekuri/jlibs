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
import jlibs.nblr.editor.UsagesDialog;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Santhosh Kumar T
 */
public class ScenePopupProvider implements PopupMenuProvider{
    private RuleScene scene;

    public ScenePopupProvider(RuleScene scene){
        this.scene = scene;
    }

    public JPopupMenu getPopupMenu(Widget widget, Point localLocation){
        JPopupMenu popup = new JPopupMenu();
        popup.add(new GenerateParserAction(scene));
        popup.add(new GenerateHandlerAction(scene));
        popup.add(new GenerateXMLAction(scene));
        popup.add(new AbstractAction("Usages..."){
            @Override
            public void actionPerformed(ActionEvent ae){
                new UsagesDialog(SwingUtilities.getWindowAncestor(scene.getView()), scene).setVisible(true);
            }
        });

        return popup;
    }
}


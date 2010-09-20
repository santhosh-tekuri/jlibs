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
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Santhosh Kumar T
 */
public class NodePopupProvider implements PopupMenuProvider{
    private RuleScene scene;

    public NodePopupProvider(RuleScene scene){
        this.scene = scene;
    }

    private Node node;
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation){
        node = (Node)scene.findObject(widget);

        JMenu insertNodeMenu = new JMenu("Insert Node");
        insertNodeMenu.add(new InsertBeforeNodeAction(scene, node));
        insertNodeMenu.add(new InsertAfterNodeAction(scene, node));
        insertNodeMenu.add(new AddBranchAction(scene, node));

        JMenu insertStringMenu = new JMenu("Insert String");
        insertStringMenu.add(new InsertStringBeforeNodeAction(scene, node));
        insertStringMenu.add(new InsertStringAfterNodeAction(scene, node));
        insertStringMenu.add(new AddStringBranchAction(scene, node));

        JMenu actionMenu = new JMenu("Set Action");
        actionMenu.add(new AssignBufferAction(scene, node));
        actionMenu.add(new AssignPublishAction(scene, node));
        actionMenu.add(new AssignEventAction(scene, node));
        actionMenu.addSeparator();
        actionMenu.add(new ClearAction(scene, node));

        JPopupMenu popup = new JPopupMenu();
        popup.add(insertNodeMenu);
        popup.add(insertStringMenu);
        popup.add(actionMenu);
        popup.addSeparator();
        popup.add(new ChoiceAction("Delete", deleteSinkAction));

        return popup;
    }
    
    private Action deleteSinkAction = new AbstractAction(){
        @Override
        public void actionPerformed(ActionEvent ae){
            for(Edge edge: node.incoming()){
                edge.setSource(null);
                edge.setTarget(null);
            }
            scene.refresh();
        }

        @Override
        public boolean isEnabled(){
            if(scene.getRule().node==node)
                return false;
            
            for(Edge edge: node.outgoing){
                if(!edge.loop())
                    return false;
            }
            return true;
        }
    };
}

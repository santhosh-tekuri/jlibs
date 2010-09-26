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
        popup.add(lookAheadAction);
        popup.addSeparator();
        popup.add(new ChoiceAction("Delete",
            deleteSourceAction,
            deleteSinkAction,
            deleteNodeWithEmptyOutgoingEdges,
            deleteNodeWithEmptyIncomingEdges
        ));

        return popup;
    }
    
    private Action lookAheadAction = new AbstractAction("Set LookAhead..."){
        @Override
        public void actionPerformed(ActionEvent ae){
            String lookAhead = JOptionPane.showInputDialog(scene.getView(), "LookAhead", String.valueOf(node.lookAhead));
            if(lookAhead!=null){
                int la = Integer.parseInt(lookAhead);
                if(la<1){
                    JOptionPane.showMessageDialog(scene.getView(), "LookAhead must be greater than zero");
                    return;
                }
                node.lookAhead = la;
                scene.layout(node);
            }
        }
    };

    private Action deleteSourceAction = new AbstractAction(){
        @Override
        public void actionPerformed(ActionEvent ae){
            scene.getRule().node = newSource();
            for(Edge edge: node.outgoing())
                edge.delete();
            scene.refresh();
        }

        private Node newSource(){
            Node newSource = null;
            if(scene.getRule().node==node){
                for(Edge outgoing: node.outgoing){
                    if(!outgoing.loop()){
                        for(Edge incoming: outgoing.target.incoming){
                            if(!incoming.loop()){
                                break;
                            }
                        }
                        if(newSource!=null)
                            return null;
                        newSource = outgoing.target;
                    }
                }
            }
            return newSource;
        }

        @Override
        public boolean isEnabled(){
            return newSource()!=null;
        }
    };
    
    private Action deleteSinkAction = new AbstractAction(){
        @Override
        public void actionPerformed(ActionEvent ae){
            for(Edge edge: node.incoming())
                edge.delete();
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

    private Action deleteNodeWithEmptyOutgoingEdges = new AbstractAction(){
        @Override
        public void actionPerformed(ActionEvent ae){
            for(Edge incoming: node.incoming()){
                if(!incoming.loop()){
                    for(Edge outgoing: node.outgoing()){
                        if(!outgoing.loop()){
                            Edge newEdge = incoming.source.addEdgeTo(outgoing.target);
                            newEdge.matcher = incoming.matcher;
                            newEdge.rule = incoming.rule;
                            incoming.delete();
                        }
                    }
                }
            }
            for(Edge outgoing: node.outgoing()){
                if(!outgoing.loop())
                    outgoing.setTarget(null);
            }
            scene.refresh();
        }

        @Override
        public boolean isEnabled(){
            if(scene.getRule().node==node)
                return false;
            
            for(Edge edge: node.outgoing){
                if(!edge.loop()){
                    if(edge.matcher!=null || edge.rule!=null)
                        return false;
                }
            }
            return true;
        }
    };
    
    private Action deleteNodeWithEmptyIncomingEdges = new AbstractAction(){
        @Override
        public void actionPerformed(ActionEvent ae){
            for(Edge outgoing: node.outgoing()){
                if(!outgoing.loop()){
                    for(Edge incoming: node.incoming()){
                        if(!incoming.loop()){
                            Edge newEdge = outgoing.target.addEdgeFrom(incoming.source);
                            newEdge.matcher = outgoing.matcher;
                            newEdge.rule = outgoing.rule;
                            outgoing.delete();
                        }
                    }
                }
            }
            for(Edge incoming: node.incoming()){
                if(!incoming.loop())
                    incoming.setSource(null);
            }
            scene.refresh();
        }

        @Override
        public boolean isEnabled(){
            if(scene.getRule().node==node)
                return false;
            
            for(Edge edge: node.incoming){
                if(!edge.loop()){
                    if(edge.matcher!=null || edge.rule!=null)
                        return false;
                }
            }
            return true;
        }
    };
}

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

import jlibs.nblr.editor.MatcherChooser;
import jlibs.nblr.editor.RuleChooser;
import jlibs.nblr.editor.RuleScene;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Rule;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Santhosh Kumar T
 */
public class EdgePopupProvider implements PopupMenuProvider{
    private RuleScene scene;
    public EdgePopupProvider(RuleScene scene){
        this.scene = scene;
    }

    private Edge edge;
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation){
        edge = (Edge)scene.findObject(widget);

        JMenu insertMenu = new JMenu("Insert Node");
        insertMenu.add(insertBeforeEdgeAction);
        insertMenu.add(insertAfterEdgeAction);

        JPopupMenu popup = new JPopupMenu();
        popup.add(insertMenu);
        popup.add(setMatcherAction);
        popup.add(setRuleAction);
        popup.add(toggleFallBackAction);
        popup.add(inlineRuleAction);
        popup.add(clearAction);
        
        int count = edge.source.outgoing.size();
        for(Edge e: edge.source.outgoing){
            if(e.loop())
                count--;
        }
        if(count>1){
            popup.addSeparator();
            popup.add(moveUpAction);
            popup.add(moveDownAction);
        }

        popup.addSeparator();
        popup.add(deleteEdgeAction);
        return popup;
    }

    private Action setMatcherAction = new AbstractAction("Set Matcher..."){
        @Override
        public void actionPerformed(ActionEvent ae){
            Window owner = SwingUtilities.getWindowAncestor(scene.getView());
            Matcher matcher = MatcherChooser.prompt(owner, scene.getSyntax());
            if(matcher!=null){
                edge.matcher = matcher;
                edge.rule = null;
                scene.refresh();
            }
        }
    };

    private Action setRuleAction = new AbstractAction("Set Rule..."){
        @Override
        public void actionPerformed(ActionEvent ae){
            Window owner = SwingUtilities.getWindowAncestor(scene.getView());
            Rule rule = RuleChooser.prompt(owner, scene.getSyntax());
            if(rule!=null){
                edge.rule = rule;
                edge.matcher = null;
                scene.refresh();
            }
        }

        @Override
        public boolean isEnabled(){
            return scene.getSyntax().rules.size()>1;
        }
    };

    private Action clearAction = new AbstractAction("Clear..."){
        @Override
        public void actionPerformed(ActionEvent ae){
            edge.matcher = null;
            edge.rule = null;
            edge.fallback = false;
            scene.refresh();
        }

        @Override
        public boolean isEnabled(){
            return edge.matcher!=null || edge.rule!=null;
        }
    };

    private Action insertAfterEdgeAction = new AbstractAction("After This"){
        @Override
        public void actionPerformed(ActionEvent ae){
            Node target = edge.target;
            Node newNode = new Node();
            newNode.addEdgeTo(target);
            edge.setTarget(newNode);
            scene.refresh();
        }
    };

    private Action insertBeforeEdgeAction = new AbstractAction("Before This"){
        @Override
        public void actionPerformed(ActionEvent ae){
            Node source = edge.source;
            Node newNode = new Node();
            newNode.addEdgeFrom(source);
            edge.setSource(newNode);
            scene.refresh();
        }
    };

    private Action deleteEdgeAction = new AbstractAction("Delete"){
        @Override
        public void actionPerformed(ActionEvent ae){
            edge.setSource(null);
            edge.setTarget(null);
            scene.refresh();
        }

        @Override
        public boolean isEnabled(){
            if(edge.loop())
                return true;
            else{
                if(edge.target==scene.getRule().node)
                    return true;

                // if node has atleast one incoming edge which is not loop, then we can delete
                for(Edge e: edge.target.incoming()){
                    if(e!=edge && !e.loop())
                        return true;
                }
            }
            return false;
        }
    };

    private Action toggleFallBackAction = new AbstractAction("Toggle Fallback"){
        @Override
        public void actionPerformed(ActionEvent ae){
            if(!edge.fallback){
                for(Edge e: edge.source.outgoing){
                    if(e!=edge)
                        e.fallback = false;
                }
            }
            edge.fallback = !edge.fallback;
            scene.refresh();
        }
    };

    private Action inlineRuleAction = new AbstractAction("Inline Rule"){
        @Override
        public void actionPerformed(ActionEvent ae){
            Rule rule = edge.rule.copy();
            edge.source.addEdgeTo(rule.node);
            for(Node node: rule.nodes()){
                boolean sink = true;
                for(Edge outgoing: node.outgoing){
                    if(!outgoing.loop()){
                        sink = false;
                        break;
                    }
                }
                if(sink)
                    node.addEdgeTo(edge.target);
            }
            edge.delete();
            scene.refresh();
        }

        @Override
        public boolean isEnabled(){
            return edge.rule!=null;
        }
    };

    public Action moveUpAction = new AbstractAction("Move Up"){
        @Override
        public void actionPerformed(ActionEvent ae){
            int index = edge.source.outgoing.indexOf(edge);
            int another = index;
            do{
                another--;
            }while(edge.source.outgoing.get(another).loop());

            edge.source.outgoing.set(index, edge.source.outgoing.get(another));
            edge.source.outgoing.set(another, edge);
            scene.refresh();
        }

        @Override
        public boolean isEnabled(){
            int another = edge.source.outgoing.indexOf(edge);
            do{
                another--;
            }while(another>=0 && edge.source.outgoing.get(another).loop());
            return another>=0;
        }
    };

    public Action moveDownAction = new AbstractAction("Move Down"){
        @Override
        public void actionPerformed(ActionEvent ae){
            int index = edge.source.outgoing.indexOf(edge);
            int another = index;
            do{
                another++;
            }while(edge.source.outgoing.get(another).loop());

            edge.source.outgoing.set(index, edge.source.outgoing.get(another));
            edge.source.outgoing.set(another, edge);
            scene.refresh();
        }

        @Override
        public boolean isEnabled(){
            int another = edge.source.outgoing.indexOf(edge);
            do{
                another++;
            }while(another<edge.source.outgoing.size() && edge.source.outgoing.get(another).loop());
            return another<edge.source.outgoing.size();
        }
    };
}



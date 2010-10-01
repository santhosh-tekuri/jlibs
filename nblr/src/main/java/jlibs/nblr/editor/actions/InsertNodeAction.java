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
import jlibs.nblr.matchers.Any;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Santhosh Kumar T
 */
public abstract class InsertNodeAction extends AbstractAction{
    protected RuleScene scene;

    public InsertNodeAction(String name, RuleScene scene){
        super(name);
        this.scene = scene;
    }

    @Override
    public void actionPerformed(ActionEvent ae){
        if(insert())
            scene.refresh();
    }

    protected abstract boolean insert();
}

class InsertBeforeNodeAction extends InsertNodeAction{
    private Node node;
    public InsertBeforeNodeAction(RuleScene scene, Node node){
        super("Before This", scene);
        this.node = node;
    }

    @Override
    protected boolean insert(){
        Node newNode = new Node();
        for(Edge edge: node.incoming()){
            if(!edge.loop())
                edge.setTarget(newNode);
        }
        newNode.addEdgeTo(node);
        if(scene.getRule().node==node)
            scene.getRule().node = newNode;

        return true;
    }
}

class InsertStringBeforeNodeAction extends InsertNodeAction{
    private Node node;
    public InsertStringBeforeNodeAction(RuleScene scene, Node node){
        super("Before This", scene);
        this.node = node;
    }

    @Override
    protected boolean insert(){
        String str = JOptionPane.showInputDialog("String");
        if(str!=null){
            boolean startingNode = scene.getRule().node==node;
            for(int i=str.length()-1; i>=0; i--){
                Node newNode = new Node();
                for(Edge edge: node.incoming()){
                    if(!edge.loop())
                        edge.setTarget(newNode);
                }
                newNode.addEdgeTo(node).matcher = new Any(str.charAt(i));
                node = newNode;
            }
            if(startingNode)
                scene.getRule().node = node;
            return true;
        }else
            return false;
    }
}

class InsertAfterNodeAction extends InsertNodeAction{
    private Node node;
    public InsertAfterNodeAction(RuleScene scene, Node node){
        super("After This", scene);
        this.node = node;
    }

    @Override
    protected boolean insert(){
        Node newNode = new Node();
        for(Edge edge: node.outgoing()){
            if(!edge.loop())
                edge.setSource(newNode);
        }
        newNode.addEdgeFrom(node);

        return true;
    }
}

class InsertStringAfterNodeAction extends InsertNodeAction{
    private Node node;
    public InsertStringAfterNodeAction(RuleScene scene, Node node){
        super("After This", scene);
        this.node = node;
    }

    @Override
    protected boolean insert(){
        String str = JOptionPane.showInputDialog("String");
        if(str!=null){
            for(int i=0; i<str.length(); i++){
                Node newNode = new Node();
                for(Edge edge: node.outgoing()){
                    if(!edge.loop())
                        edge.setSource(newNode);
                }
                newNode.addEdgeFrom(node).matcher = new Any(str.charAt(i));
                node = newNode;
            }
            return true;
        }else
            return false;
    }
}

class AddBranchAction extends InsertNodeAction{
    private Node node;
    public AddBranchAction(RuleScene scene, Node node){
        super("As Branch", scene);
        this.node = node;
    }

    @Override
    protected boolean insert(){
        node.addEdgeTo(new Node());
        return true;
    }
}

class AddStringBranchAction extends InsertNodeAction{
    private Node node;
    public AddStringBranchAction(RuleScene scene, Node node){
        super("As Branch", scene);
        this.node = node;
    }

    @Override
    protected boolean insert(){
        String str = JOptionPane.showInputDialog("String");
        if(str!=null){
            for(int i=0; i<str.length(); i++){
                Node newNode = new Node();
                node.addEdgeTo(newNode).matcher = new Any(str.charAt(i));
                node = newNode;
            }
            return true;
        }else
            return false;
    }
}

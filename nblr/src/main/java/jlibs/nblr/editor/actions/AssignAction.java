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

import jlibs.nblr.actions.Action;
import jlibs.nblr.actions.*;
import jlibs.nblr.editor.RuleScene;
import jlibs.nblr.rules.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Santhosh Kumar T
 */
abstract class AssignAction extends AbstractAction{
    private RuleScene scene;
    protected Node node;
    
    protected AssignAction(RuleScene scene, Node node, String name){
        super(name);
        this.scene = scene;
        this.node = node;
    }

    public final void actionPerformed(ActionEvent ae){
        jlibs.nblr.actions.Action action = action();
        if(action!=null){
            node.action = action;
            scene.layout(node);
        }
    }

    protected abstract jlibs.nblr.actions.Action action();
}

class AssignBufferAction extends AssignAction{
    AssignBufferAction(RuleScene scene, Node node){
        super(scene, node, "Buffer");
    }

    @Override
    protected Action action(){
        return BufferAction.INSTANCE;
    }
}

class AssignPublishAction extends AssignAction{
    AssignPublishAction(RuleScene scene, Node node){
        super(scene, node, "Publish...");
    }

    @Override
    protected Action action(){
        String name = JOptionPane.showInputDialog("Name");
        if(name==null)
            return null;
        else{
            int begin = 0;
            int end = 0;
            if(name.endsWith("]")){
                int nameEnd = name.indexOf("[");
                if(nameEnd==-1){
                    JOptionPane.showMessageDialog(null, "missing '['");
                    return action();
                }
                String str = name.substring(nameEnd+1, name.length()-1);
                int comma = str.indexOf(',');
                if(comma==-1){
                    JOptionPane.showMessageDialog(null, "missing ','");
                    return action();
                }
                try{
                    begin = Integer.parseInt(str.substring(0, comma));
                }catch(NumberFormatException e){
                    JOptionPane.showMessageDialog(null, "invalid begin index");
                    return action();
                }
                try{
                    end = Integer.parseInt(str.substring(comma+1));
                }catch(NumberFormatException e){
                    JOptionPane.showMessageDialog(null, "invalid end index");
                    return action();
                }
                name = name.substring(0, nameEnd);
            }
            return new PublishAction(name, Math.abs(begin), Math.abs(end));
        }
    }
}

class AssignEventAction extends AssignAction{
    AssignEventAction(RuleScene scene, Node node){
        super(scene, node, "Event...");
    }

    @Override
    protected Action action(){
        String name = JOptionPane.showInputDialog("Name");
        return name==null ? null : new EventAction(name);
    }
}

class AssignErrorAction extends AssignAction{
    AssignErrorAction(RuleScene scene, Node node){
        super(scene, node, "Error...");
    }

    @Override
    protected Action action(){
        String errorMessage = JOptionPane.showInputDialog("Error Message");
        return errorMessage==null ? null : new ErrorAction(errorMessage);
    }
}

class ClearAction extends AbstractAction{
    private RuleScene scene;
    protected Node node;

    ClearAction(RuleScene scene, Node node){
        super("Clear");
        this.scene = scene;
        this.node = node;
    }

    public void actionPerformed(ActionEvent ae){
        node.action = null;
        scene.layout(node);
    }

    @Override
    public boolean isEnabled(){
        return node.action!=null;
    }
}

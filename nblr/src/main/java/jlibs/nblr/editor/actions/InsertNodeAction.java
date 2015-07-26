/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.nblr.editor.actions;

import jlibs.nblr.editor.RuleScene;
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
            scene.getRule().insertStringBefore(node, str);
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
            scene.getRule().insertStringAfter(node, str);
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
            scene.getRule().addStringBranch(node, str);
            return true;
        }else
            return false;
    }
}

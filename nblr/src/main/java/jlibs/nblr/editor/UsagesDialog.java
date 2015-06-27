/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

package jlibs.nblr.editor;

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Sequence;
import jlibs.core.graph.Visitor;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.nblr.rules.Rule;
import jlibs.swing.tree.NavigatorTreeModel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class UsagesDialog extends TreeSelectionDialog{
    private RuleScene scene;

    public UsagesDialog(Window owner, RuleScene scene){
        super(owner, "Usages of "+scene.getRule());
        this.scene = scene;
        cancelAction.putValue(Action.NAME, "Close");
        createContents();
    }

    @Override
    protected NavigatorTreeModel treeModel(){
        return new NavigatorTreeModel(scene.getRule(), new Navigator<Rule>(){
            @Override
            public Sequence<? extends Rule> children(Rule elem){
                return new IterableSequence<Rule>(scene.getSyntax().usages(elem));
            }
        });
    }

    protected void onOK(){
        super.onOK();
        scene.setRule(scene.getSyntax(), (Rule)tree.getSelectionPath().getLastPathComponent());
    }

    @Override
    protected Action defaultAction(){
        return cancelAction;
    }

    @Override
    protected Action[] actions(){
        return new Action[]{ cancelAction };
    }

    @Override
    protected Visitor<Object, String> displayVisitor(){
        return null;
    }
}

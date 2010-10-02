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

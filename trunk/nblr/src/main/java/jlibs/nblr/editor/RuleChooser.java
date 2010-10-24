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
import jlibs.core.graph.sequences.EmptySequence;
import jlibs.core.graph.sequences.IterableSequence;
import jlibs.core.lang.ImpossibleException;
import jlibs.nblr.Syntax;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Rule;
import jlibs.nblr.rules.RuleTarget;
import jlibs.swing.tree.NavigatorTreeModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Santhosh Kumar T
 */
public class RuleChooser extends TreeSelectionDialog{
    private Syntax syntax;

    public RuleChooser(Window owner, Syntax syntax){
        super(owner, "Rule Chooser");
        this.syntax = syntax;
        createContents();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected NavigatorTreeModel treeModel(){
        return new NavigatorTreeModel(syntax, new RuleNavigator());
    }

    @Override
    protected Visitor<Object, String> displayVisitor(){
        return new Visitor<Object, String>(){
            @Override
            public String visit(Object elem){
                if(elem instanceof Rule)
                    return elem.toString();
                else if(elem instanceof Node)
                    return ((Node)elem).name;
                else
                    return elem.getClass().getName();
            }
        };
    }

    protected boolean showRoot(){
        return false;
    }

    public static RuleTarget prompt(Window owner, Syntax syntax){
        RuleChooser chooser = new RuleChooser(owner, syntax);
        chooser.setVisible(true);
        if(chooser.ok){
            RuleTarget ruleTarget = new RuleTarget();
            Object path[] = chooser.tree.getSelectionPath().getPath();
            ruleTarget.rule = (Rule)path[1];
            if(path.length==3)
                ruleTarget.name = ((Node)path[2]).name;
            return ruleTarget;
        }else
            return null;
    }
}

class RuleNavigator implements Navigator{
    @Override
    public Sequence children(Object elem){
        if(elem instanceof Syntax){
            Syntax syntax = (Syntax)elem;
            return new IterableSequence<Rule>(syntax.rules.values());
        }else if(elem instanceof Rule){
            Rule rule = (Rule)elem;
            ArrayList<Node> nodes = rule.nodes();
            Iterator<Node> iter = nodes.iterator();
            while(iter.hasNext()){
                Node node = iter.next();
                if(node.name==null)
                    iter.remove();
            }
            return new IterableSequence<Node>(nodes);
        }else if(elem instanceof Node)
            return EmptySequence.getInstance();
        else
            throw new ImpossibleException(elem.getClass().getName());
    }
}

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

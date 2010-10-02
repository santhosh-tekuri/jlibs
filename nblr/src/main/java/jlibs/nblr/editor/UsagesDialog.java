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

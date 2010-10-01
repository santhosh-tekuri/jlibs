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
import jlibs.swing.tree.MyTreeCellRenderer;
import jlibs.swing.tree.NavigatorTreeModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Santhosh Kumar T
 */
public class RuleChooser extends JDialog implements TreeSelectionListener{
    private JTree tree;

    @SuppressWarnings({"unchecked"})
    public RuleChooser(Window owner, Syntax syntax){
        super(owner, "Rule Chooser");
        setModal(true);

        JPanel contents = (JPanel)getContentPane();
        contents.setLayout(new BorderLayout(0, 10));
        contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tree = new JTree(new NavigatorTreeModel(syntax, new RuleNavigator()));
        MyTreeCellRenderer cellRenderer = new MyTreeCellRenderer();
        cellRenderer.setTextConvertor(new Visitor<Object, String>(){
            @Override
            public String visit(Object elem){
                if(elem instanceof Rule)
                    return ((Rule)elem).name;
                else if(elem instanceof Node)
                    return ((Node)elem).name;
                else
                    return elem.getClass().getName();
            }
        });
        tree.setCellRenderer(cellRenderer);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setFont(Util.FIXED_WIDTH_FONT);
        contents.add(new JScrollPane(tree));
        tree.addTreeSelectionListener(this);
        valueChanged(null);
        tree.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent me){
                if(me.getClickCount()>1)
                    okAction.actionPerformed(null);
            }
        });
        
        JPanel buttons = new JPanel(new GridLayout(1, 0));
        JButton okButton;
        buttons.add(okButton=new JButton(okAction));
        buttons.add(new JButton(cancelAction));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttons, BorderLayout.EAST);
        getContentPane().add(southPanel, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(cancelAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JRootPane.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().setDefaultButton(okButton);

        tree.setVisibleRowCount(15);
        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void valueChanged(TreeSelectionEvent tse){
        okAction.setEnabled(tree.getSelectionPath()!=null);
    }

    boolean ok = false;
    private void onOK(){
        ok = true;
    }

    private Action okAction = new AbstractAction("Ok"){
        @Override
        public void actionPerformed(ActionEvent ae){
            onOK();
            RuleChooser.this.setVisible(false);
        }
    };

    @SuppressWarnings({"FieldCanBeLocal"})
    private Action cancelAction = new AbstractAction("Cancel"){
        @Override
        public void actionPerformed(ActionEvent ae){
            RuleChooser.this.setVisible(false);
        }
    };

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

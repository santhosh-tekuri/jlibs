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

import jlibs.nblr.Syntax;
import jlibs.nblr.rules.Rule;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author Santhosh Kumar T
 */
public class RuleChooser extends JDialog implements ListSelectionListener{
    private JList list;

    public RuleChooser(Window owner, Syntax syntax, Rule ruleToBeExcluded){
        super(owner, "Rule Choser");
        setModal(true);

        JPanel contents = (JPanel)getContentPane();
        contents.setLayout(new BorderLayout(0, 10));
        contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ArrayList<Rule> rules = new ArrayList<Rule>(syntax.rules.values());
        rules.remove(ruleToBeExcluded);
        list = new JList(rules.toArray());
        list.setFont(Util.FIXED_WIDTH_FONT);
        contents.add(new JScrollPane(list));
        list.addListSelectionListener(this);
        valueChanged(null);
        list.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent me){
                if(me.getClickCount()>1)
                    okAction.actionPerformed(null);
            }
        });
        
        JPanel buttons = new JPanel(new GridLayout(1, 0));
        buttons.add(new JButton(okAction));
        buttons.add(new JButton(cancelAction));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttons, BorderLayout.EAST);
        getContentPane().add(southPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void valueChanged(ListSelectionEvent lse){
        okAction.setEnabled(list.getSelectedIndex()!=-1);
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

    public static Rule prompt(Window owner, Syntax syntax, Rule ruleToBeExcluded){
        RuleChooser chooser = new RuleChooser(owner, syntax, ruleToBeExcluded);
        chooser.setVisible(true);
        if(chooser.ok)
            return (Rule)chooser.list.getSelectedValue();
        else
            return null;
    }
}

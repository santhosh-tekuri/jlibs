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
import jlibs.nblr.matchers.Matcher;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Santhosh Kumar T
 */
public class MatcherChooser extends JDialog implements ListSelectionListener{
    private Syntax syntax;
    private JTable table;

    public MatcherChooser(Window owner, Syntax syntax){
        super(owner, "Matcher Choser");
        setModal(true);
        this.syntax = syntax;

        JPanel contents = (JPanel)getContentPane();
        contents.setLayout(new BorderLayout(0, 10));
        contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table = new JTable(new MatcherTableModel());
        table.setFont(Util.FIXED_WIDTH_FONT);
        contents.add(new JScrollPane(table));
        table.getSelectionModel().addListSelectionListener(this);
        valueChanged(null);
        table.addMouseListener(new MouseAdapter(){
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
        okAction.setEnabled(table.getSelectedRowCount()>0);
    }

    boolean ok = false;
    private void onOK(){
        ok = true;
    }

    private Action okAction = new AbstractAction("Ok"){
        @Override
        public void actionPerformed(ActionEvent ae){
            onOK();
            MatcherChooser.this.setVisible(false);
        }
    };

    @SuppressWarnings({"FieldCanBeLocal"})
    private Action cancelAction = new AbstractAction("Cancel"){
        @Override
        public void actionPerformed(ActionEvent ae){
            MatcherChooser.this.setVisible(false);
        }
    };

    class MatcherTableModel extends AbstractTableModel{
        @Override
        public int getRowCount(){
            return syntax.matchers.size();
        }

        @Override
        public int getColumnCount(){
            return 2;
        }

        @Override
        public String getColumnName(int column){
            return column==0 ? "Name" : "Regex";
        }

        @Override
        public Object getValueAt(int row, int col){
            Matcher matcher = getMatcher(row);
            assert matcher!=null;
            return col==0 ? matcher.name : matcher.toString();
        }
    }

    private Matcher getMatcher(int row){
        Matcher matcher = null;
        for(Matcher m: syntax.matchers.values()){
            row--;
            if(row<0){
                matcher = m;
                break;
            }
        }
        return matcher;
    }

    public static Matcher prompt(Window owner, Syntax syntax){
        MatcherChooser chooser = new MatcherChooser(owner, syntax);
        chooser.setVisible(true);
        if(chooser.ok)
            return chooser.getMatcher(chooser.table.getSelectedRow());
        else
            return null;
    }
}

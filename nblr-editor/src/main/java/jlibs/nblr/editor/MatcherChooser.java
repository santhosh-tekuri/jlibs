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

import jlibs.core.lang.ImpossibleException;
import jlibs.nblr.Syntax;
import jlibs.nblr.matchers.Any;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.matchers.Not;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Santhosh Kumar T
 */
public class MatcherChooser extends JDialog{
    private Syntax syntax;
    private JTable table;
    private JTextField matcherSyntax = new JTextField();

    public MatcherChooser(Window owner, Syntax syntax){
        super(owner, "Matcher Choser");
        setModal(true);
        this.syntax = syntax;

        JPanel newMatcherPanel = new JPanel(new BorderLayout());
        newMatcherPanel.add(new JLabel("New Matcher"), BorderLayout.WEST);
        matcherSyntax.setFont(Util.FIXED_WIDTH_FONT);
        newMatcherPanel.add(matcherSyntax, BorderLayout.CENTER);
        matcherSyntax.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de){
                textChanged(de);
            }

            @Override
            public void removeUpdate(DocumentEvent de){
                textChanged(de);
            }

            private void textChanged(DocumentEvent de){
                try{
                    if(de.getDocument().getText(0, de.getDocument().getLength()).trim().length()>0)
                        table.clearSelection();
                    updateActions();
                }catch(BadLocationException ex){
                    throw new ImpossibleException(ex);
                }
            }
            @Override
            public void changedUpdate(DocumentEvent de){}
        });

        table = new JTable(new MatcherTableModel());
        table.setFont(Util.FIXED_WIDTH_FONT);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse){
                if(table.getSelectedRowCount()>0)
                    matcherSyntax.setText("");
                updateActions();
            }
        });
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent me){
                if(me.getClickCount()>1)
                    okAction.actionPerformed(null);
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        centerPanel.add(newMatcherPanel, BorderLayout.SOUTH);
        
        JPanel buttons = new JPanel(new GridLayout(1, 0));
        JButton okButton;
        buttons.add(okButton=new JButton(okAction));
        buttons.add(new JButton(cancelAction));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttons, BorderLayout.EAST);

        JPanel contents = (JPanel)getContentPane();
        contents.setLayout(new BorderLayout(0, 10));
        contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contents.add(centerPanel);
        getContentPane().add(southPanel, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(cancelAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JRootPane.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().setDefaultButton(okButton);

        pack();
        setLocationRelativeTo(null);
        updateActions();
    }

    private void updateActions(){
        okAction.setEnabled(table.getSelectedRowCount()>0 || matcherSyntax.getText().trim().length()>0);
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
        if(chooser.ok){
            int row = chooser.table.getSelectedRow();
            if(row==-1){
                String text = chooser.matcherSyntax.getText().trim();
                text = text.substring(1, text.length() - 1);
                if(text.startsWith("^"))
                    return new Not(new Any(text.substring(1)));
                else
                    return new Any(text);
            }else
                return chooser.getMatcher(row);
        }else
            return null;
    }
}

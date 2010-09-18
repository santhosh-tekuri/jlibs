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
import jlibs.nblr.SyntaxBinding;
import jlibs.nblr.editor.debug.Debugger;
import jlibs.nblr.matchers.Matcher;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Rule;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.sax.binding.BindingHandler;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.widget.Widget;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import static jlibs.nblr.matchers.Matcher.*;

/**
 * @author Santhosh Kumar T
 */
public class NBLREditor extends JFrame{
    private Syntax syntax;
    private RuleScene scene;
    private JComboBox combo;

    public NBLREditor(Syntax syntax){
        super("NBLR Editor");
        
        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        fileMenu.add(newAction).setAccelerator(KeyStroke.getKeyStroke('N', ctrl));
        fileMenu.add(openAction).setAccelerator(KeyStroke.getKeyStroke('O', ctrl));
        fileMenu.add(saveAction).setAccelerator(KeyStroke.getKeyStroke('S', ctrl));
        fileMenu.add(saveAsAction).setAccelerator(KeyStroke.getKeyStroke('S', ctrl|InputEvent.SHIFT_DOWN_MASK));
        menubar.add(fileMenu);
        setJMenuBar(menubar);

        combo = createRulesCombo();
        scene = new RuleScene(null, new EditProvider(){
            @Override
            public void edit(Widget widget){
                Edge edge = Util.edge(widget);
                if(edge.rule!=null){
                    history.push(new SceneState(scene));
                    upAction.setEnabled(true);
                    userRuleSelection = false;
                    combo.setSelectedItem(edge.rule);
                    userRuleSelection = true;
                }
            }
        }){
            @Override
            public void setRule(Syntax syntax, Rule rule){
                super.setRule(syntax, rule);
                if(rule!=null)
                    combo.setSelectedIndex(rule.id);
            }
        };
        
        JPanel comboPanel = new JPanel(new BorderLayout());
        comboPanel.add(new JLabel("Rules"), BorderLayout.WEST);
        comboPanel.add(combo, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(comboPanel, BorderLayout.CENTER);
        topPanel.add(Util.toolbar(upAction, newRuleAction), BorderLayout.EAST);
        upAction.setEnabled(false);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(scene.createView()), new Debugger(scene));
        split.setBorder(null);
        split.addAncestorListener(new AncestorListener(){
            public void ancestorAdded(AncestorEvent event){
                JSplitPane split = (JSplitPane)event.getSource();
                JTextArea textArea = new JTextArea(12, 10);
                textArea.setFont(Util.FIXED_WIDTH_FONT);
                split.setDividerLocation(split.getHeight()-textArea.getPreferredSize().height-split.getDividerSize()-2);
                split.removeAncestorListener(this);
            }

            public void ancestorRemoved(AncestorEvent event){
            }

            public void ancestorMoved(AncestorEvent event){
            }
        });

        JPanel contents = (JPanel)getContentPane();
        ((BorderLayout)contents.getLayout()).setVgap(5);
        contents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contents.add(topPanel, BorderLayout.NORTH);
        contents.add(split, BorderLayout.CENTER);

        setSize(800, 600);
        setLocationRelativeTo(null);
        showSyntax(syntax);
    }

    private JComboBox createRulesCombo(){
        JComboBox combo = new JComboBox();
        combo.addItemListener(new ItemListener(){
            public void itemStateChanged(final ItemEvent event){
                switch(event.getStateChange()){
                    case ItemEvent.DESELECTED:
                        scene.setRule(syntax, null);
                        break;
                    case ItemEvent.SELECTED:
                        scene.setRule(syntax, (Rule)event.getItem());
                        ((JViewport)scene.getView().getParent()).setViewPosition(new Point(0, 0));
                        if(userRuleSelection){
                            history.clear();
                            upAction.setEnabled(false);
                        }
                }
            }
        });
        return combo;
    }

    private void showSyntax(Syntax syntax){
        this.syntax = syntax;
        combo.setModel(new DefaultComboBoxModel(syntax.rules.values().toArray()));
        if(syntax.rules.size()>0)
            scene.setRule(syntax, syntax.rules.values().iterator().next());
    }

    /*-------------------------------------------------[ Actions ]---------------------------------------------------*/

    private ImageIcon icon(String name){
        return new ImageIcon(getClass().getResource(name));
    }
    
    private File file;
    
    @SuppressWarnings({"FieldCanBeLocal"})
    private Action newAction = new AbstractAction("New"){
        public void actionPerformed(ActionEvent ae){
            file = null;
            Syntax syntax = new Syntax();
            Rule rule = new Rule();
            rule.node = new Node();
            syntax.add("RULE1", rule);
            showSyntax(syntax);
        }
    };

    @SuppressWarnings({"FieldCanBeLocal"})
    private Action openAction = new AbstractAction("Open..."){
        public void actionPerformed(ActionEvent ae){
            JFileChooser chooser = new JFileChooser();
            if(file!=null)
                chooser.setCurrentDirectory(file.getParentFile());
            if(chooser.showOpenDialog(NBLREditor.this)==JFileChooser.APPROVE_OPTION){
                File selected = chooser.getSelectedFile();
                BindingHandler handler = new BindingHandler(SyntaxBinding.class);
                try{
                    showSyntax((Syntax)handler.parse(new InputSource(selected.getPath())));
                    file = selected;
                }catch(Exception ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(NBLREditor.this, ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };

    private void saveTo(File file){
        if(file==null){
            JFileChooser chooser = new JFileChooser();
            if(this.file!=null)
                chooser.setCurrentDirectory(this.file.getParentFile());
            if(chooser.showSaveDialog(NBLREditor.this)==JFileChooser.APPROVE_OPTION)
                file = chooser.getSelectedFile();
            else
                return;
        }
        try{
            XMLDocument xml = new XMLDocument(new StreamResult(file.getPath()), true, 4, null);
            xml.startDocument();
            xml.add(scene.getSyntax());
            xml.endDocument();
            this.file = file;
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(NBLREditor.this, ex.getMessage(), ex.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings({"FieldCanBeLocal"})
    private Action saveAction = new AbstractAction("Save"){
        public void actionPerformed(ActionEvent ae){
            saveTo(file);
        }
    };

    @SuppressWarnings({"FieldCanBeLocal"})
    private Action saveAsAction = new AbstractAction("Save As..."){
        public void actionPerformed(ActionEvent ae){
            saveTo(null);
        }
    };

    @SuppressWarnings({"FieldCanBeLocal"})
    private Action newRuleAction = new AbstractAction("New Rule...", icon("newRule.png")){
        public void actionPerformed(ActionEvent ae){
            String ruleName = JOptionPane.showInputDialog("Rule Name");
            if(ruleName!=null){
                Rule rule = new Rule();
                rule.node = new Node();
                syntax.add(ruleName, rule);
                combo.setModel(new DefaultComboBoxModel(syntax.rules.values().toArray()));
                if(combo.getItemCount()==1)
                    scene.setRule(syntax, rule);
                else
                    combo.setSelectedIndex(combo.getItemCount()-1);
            }
        }
    };

    @SuppressWarnings({"FieldCanBeLocal"})
    private Action upAction = new AbstractAction("Up", icon("up.png")){
        public void actionPerformed(ActionEvent ae){
            SceneState sceneState = history.pop();
            userRuleSelection = false;
            combo.setSelectedItem(sceneState.rule);
            ((JViewport)scene.getView().getParent()).setViewPosition(sceneState.position);
            userRuleSelection = true;
            setEnabled(!history.isEmpty());
        }
    };

    private final Deque<SceneState> history = new ArrayDeque<SceneState>();
    private boolean userRuleSelection = true;
    static class SceneState{
        Rule rule;
        Point position;

        SceneState(RuleScene scene){
            rule = scene.getRule();
            position = ((JViewport)scene.getView().getParent()).getViewPosition();
        }
    }

    /*-------------------------------------------------[ Test ]---------------------------------------------------*/

    @SuppressWarnings({"UnusedDeclaration"})
    public static void main(String[] args){
        Syntax syntax = new Syntax();

        Matcher GT              = syntax.add("GT",             ch('>'));
        Matcher BRACKET_CLOSE   = syntax.add("BRACKET_CLOSE",  ch(']'));
        Matcher Q               = syntax.add("Q",              ch('\''));
        Matcher DQ              = syntax.add("DQ",             ch('"'));
        Matcher DIGIT           = syntax.add("DIGIT",          range("0-9"));
        Matcher WS              = syntax.add("WS",             any(" \t\n\r"));
        Matcher ENCODING_START  = syntax.add("ENCODING_START", or(range("A-Z"), range("a-z")));
        Matcher ENCODING_PART   = syntax.add("ENCODING_PART",  or(ENCODING_START, DIGIT, any("._-")));
        Matcher CHAR            = syntax.add("CHAR",           or(any("\t\n\r"), range(" -\uD7FF"), range("\uE000-\uFFFD")/*, range("\u10000-\u10FFFF")*/));
        Matcher DASH            = syntax.add("DASH",           ch('-'));
        Matcher NDASH           = syntax.add("NDASH",          minus(CHAR, ch('-')));

//        Rule rule = new Rule();
//        rule.node = new Node();
//        syntax.add("Rule1", rule);

        NBLREditor editor = new NBLREditor(syntax);
        editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        editor.setVisible(true);
    }
}
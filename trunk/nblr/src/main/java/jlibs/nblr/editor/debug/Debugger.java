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

package jlibs.nblr.editor.debug;

import jlibs.core.annotation.processing.Printer;
import jlibs.core.io.FileUtil;
import jlibs.core.lang.ImpossibleException;
import jlibs.core.lang.StringUtil;
import jlibs.nblr.actions.BufferAction;
import jlibs.nblr.actions.ErrorAction;
import jlibs.nblr.actions.EventAction;
import jlibs.nblr.actions.PublishAction;
import jlibs.nblr.codegen.java.JavaCodeGenerator;
import jlibs.nblr.editor.RuleScene;
import jlibs.nblr.editor.Util;
import jlibs.nblr.rules.Edge;
import jlibs.nblr.rules.Node;
import jlibs.nblr.rules.Rule;
import jlibs.nbp.Chars;
import jlibs.nbp.NBHandler;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Santhosh Kumar T
 */
public class Debugger extends JPanel implements NBHandler, Observer{
    private RuleScene scene;
    private JTextArea input = new JTextArea();
    private JList ruleStackList = new JList(new DefaultListModel());

    public Debugger(RuleScene scene){
        super(new BorderLayout(5, 5));
        this.scene = scene;
        scene.ruleObservable.addObserver(this);

        JToolBar toolbar = Util.toolbar(
            runAction,
            debugAction,
            null,
            stepAction,
            runToCursorAction,
            resumeAction,
            suspendAction
        );
        add(toolbar, BorderLayout.NORTH);

        input.setFont(Util.FIXED_WIDTH_FONT);
        input.addCaretListener(new CaretListener(){
            @Override
            public void caretUpdate(CaretEvent ce){
                updateActions();
            }
        });
        add(new JScrollPane(input), BorderLayout.CENTER);

        ruleStackList.setFont(Util.FIXED_WIDTH_FONT);
        ruleStackList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse){
                Rule rule = (Rule)ruleStackList.getSelectedValue();
                RuleScene scene = Debugger.this.scene;
                if(rule!=null && scene.getRule()!=rule)
                    scene.setRule(scene.getSyntax(), rule);
            }
        });
        add(new JScrollPane(ruleStackList), BorderLayout.EAST);

        message.setFont(Util.FIXED_WIDTH_FONT);
        add(message, BorderLayout.SOUTH);

        updateActions();
    }

    private String compile(File file){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ArrayList<String> args = new ArrayList<String>();
        args.add("-d");
        args.add(file.getParentFile().getAbsolutePath());
        args.add("-s");
        args.add(file.getParentFile().getAbsolutePath());
        args.add(file.getAbsolutePath());
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        if(compiler.run(null, null, err, args.toArray(new String[args.size()]))==0)
            return null;
        return err.toString();
    }

    private DebuggableNBParser parser;
    private int inputIndex;
    private void start(){
        try{
            showMessage("");
            clearGuardedBlock();

            String parserName = "UntitledParser";

            File file = new File("temp/"+parserName+".java").getAbsoluteFile();
            FileUtil.mkdirs(file.getParentFile());
            
            JavaCodeGenerator codeGenerator = new JavaCodeGenerator(scene.getSyntax());
            codeGenerator.setParserName(parserName);
            codeGenerator.setDebuggable(DebuggableNBParser.class, getClass());

            Printer printer = new Printer(new PrintWriter(new FileWriter(file)));
            codeGenerator.generateParser(printer);
            printer.close();
            
            String error = compile(file);
            if(error!=null){
                JOptionPane.showMessageDialog(this, error);
                return;
            }

            URLClassLoader classLoader = new URLClassLoader(new URL[]{FileUtil.toURL(file.getParentFile())});
            Class clazz = classLoader.loadClass(parserName);
            parser = (DebuggableNBParser)clazz.getConstructor(getClass(), int.class).newInstance(this, scene.getRule().id);
            showMessage("Executing...");
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void step(){
        try{
            if(inputIndex<input.getDocument().getLength()){
                char ch = input.getDocument().getText(inputIndex, 1).charAt(0);
                parser.consume(new char[]{ ch }, 0, 1);
                inputIndex++;
                updateGuardedBlock();
            }else{
                parser.eof();
                stop(null, "Input Matched");
            }
        }catch(BadLocationException ex){
            throw new ImpossibleException(ex);
        }catch(Exception ex){
            stop(ex, null);
        }
    }

    private void stop(Exception ex, String message){
        parser = null;
        inputIndex = 0;
        if(ex==null){
            clearGuardedBlock();
            scene.executing((Node)null);
            showMessage(message);
        }else
            showError(ex);
    }

    /*-------------------------------------------------[ Message ]---------------------------------------------------*/
    
    private JLabel message = new JLabel();

    private void showMessage(String msg){
        message.setForeground(Color.BLUE);
        message.setText(msg);
    }

    private void showError(Exception ex){
        String text = ex.getMessage();
        if(!(ex instanceof ParseException)){
            ex.printStackTrace();
            text = "[BUG] "+text;
        }
        message.setForeground(Color.RED);
        message.setText(text);
    }

    /*-------------------------------------------------[ GuardBlock ]---------------------------------------------------*/
    
    private Highlighter.HighlightPainter consumedHighlightPainter = new NewLineHighlightPainter(Color.LIGHT_GRAY);
    private Highlighter.HighlightPainter lookAheadHighlightPainter = new NewLineHighlightPainter(Color.CYAN);
    private void updateGuardedBlock() throws BadLocationException{
        input.getHighlighter().removeAllHighlights();
        int consumed = parser.location.getCharacterOffset();
        if(consumed>inputIndex)
            throw new ImpossibleException("consumed="+consumed+" inputIndex="+inputIndex);
        if(consumed>0)
            input.getHighlighter().addHighlight(0, consumed, consumedHighlightPainter);

        if(inputIndex!=consumed)
            input.getHighlighter().addHighlight(consumed, inputIndex, lookAheadHighlightPainter);

        input.repaint();
    }

    private void clearGuardedBlock(){
        input.getHighlighter().removeAllHighlights();
        input.repaint();
    }

    /*-------------------------------------------------[ Actions ]---------------------------------------------------*/

    private void updateActions(){
        if(scene.getSyntax()!=null)
            ruleStackList.setPrototypeCellValue(scene.getSyntax().ruleProtypeWidth());
        JScrollPane scroll = (JScrollPane)ruleStackList.getParent().getParent();
        scroll.setVisible(parser!=null);
        DefaultListModel model = (DefaultListModel)ruleStackList.getModel();
        model.clear();
        if(parser!=null){
            Rule rules[] = scene.getSyntax().rules.values().toArray(new Rule[scene.getSyntax().rules.values().size()]);
            for(int i=0; i<parser.free(); i+=2)
                model.insertElementAt(rules[parser.getStack()[i]], 0);
            ruleStackList.setSelectedIndex(model.size()-1);
        }
        scroll.revalidate();
        doLayout();
        input.revalidate();

        runAction.setEnabled(parser==null);
        debugAction.setEnabled(parser==null);
        stepAction.setEnabled(parser!=null);
        resumeAction.setEnabled(parser!=null);
        suspendAction.setEnabled(parser!=null);
        runToCursorAction.setEnabled(parser!=null && inputIndex<input.getCaretPosition());
    }
    
    private ImageIcon icon(String name){
        return new ImageIcon(getClass().getResource(name));
    }

    private Action runAction = new AbstractAction("Run", icon("run.png")){
        public void actionPerformed(ActionEvent ae){
            start();
            while(parser!=null)
                step();
            updateActions();
        }
    };

    private Action debugAction = new AbstractAction("Debug", icon("debug.png")){
        public void actionPerformed(ActionEvent ae){
            start();
            updateActions();
        }
    };

    private Action stepAction = new AbstractAction("Step", icon("step.png")){
        public void actionPerformed(ActionEvent ae){
            step();
            updateActions();
        }
    };

    private Action runToCursorAction = new AbstractAction("Run to Cursor", icon("runToCursor.png")){
        public void actionPerformed(ActionEvent ae){
            while(parser!=null && inputIndex<input.getCaretPosition())
                step();
            updateActions();
        }
    };

    private Action resumeAction = new AbstractAction("Resume", icon("resume.png")){
        public void actionPerformed(ActionEvent ae){
            while(parser!=null)
                step();
            updateActions();
        }
    };

    private Action suspendAction = new AbstractAction("Stop", icon("suspend.png")){
        public void actionPerformed(ActionEvent ae){
            stop(null, "");
            updateActions();
        }
    };

    private boolean ignoreRuleChange = false;

    @Override
    public void update(Observable o, Object rule){
        if(ignoreRuleChange || rule==null)
            return;
        int ruleIndex = -1;
        ListModel model = ruleStackList.getModel();
        for(int i=model.getSize()-1; i>=0; i--){
            if(model.getElementAt(i)==rule){
                ruleIndex = i;
                break;
            }
        }
        if(ruleIndex==-1)
            ruleStackList.clearSelection();
        else{
            ruleStackList.setSelectedIndex(ruleIndex);
            ArrayList<Integer> states = new ArrayList<Integer>();
            for(int i=1; i<parser.free(); i+=2)
                states.add(0, parser.getStack()[i]);
            int state = states.get(ruleIndex);
            Node node = ((Rule)rule).nodes().get(state);
            if(ruleIndex==model.getSize()-1)
                scene.executing(node);
            else{
                for(Edge edge: node.incoming()){
                    if(edge.ruleTarget!=null && edge.ruleTarget.rule==model.getElementAt(ruleIndex+1)){
                        scene.executing(edge);
                        return;
                    }
                }
            }
        }
    }

    /*-------------------------------------------------[ Consumer ]---------------------------------------------------*/

    public void execute(int rule, int... ids) throws Exception{
        setCurrentRule(rule);
        for(int id: ids){
//            System.out.println("hitNode("+id+")");
            Node node = currentRule.nodes().get(id);
            if(node.action== BufferAction.INSTANCE){
                System.out.println("BUFFERRING");
                parser.getBuffer().push();
            }else if(node.action instanceof PublishAction){
                PublishAction action = (PublishAction)node.action;
                Chars data = parser.getBuffer().pop(action.begin, action.end);
                System.out.println(action.name+"(\""+ StringUtil.toLiteral(data, false)+"\")");
            }else if(node.action instanceof EventAction){
                EventAction action = (EventAction)node.action;
                System.out.println(action.name+"()");
            }else if(node.action instanceof ErrorAction){
                ErrorAction action = (ErrorAction)node.action;
                System.out.println("error(\""+StringUtil.toLiteral(action.errorMessage, false)+"\")");
                fatalError(action.errorMessage);
            }
            scene.executing(node);
        }
    }

    private Rule currentRule;
    private void setCurrentRule(int rule){
        if(scene.getRule().id!=rule){
            ignoreRuleChange = true;
            try{
                currentRule = (Rule)scene.getSyntax().rules.values().toArray()[rule];
                scene.setRule(scene.getSyntax(),  currentRule);
            }finally{
                ignoreRuleChange = false;
            }
        }
    }

    public void currentNode(int ruleID, int nodeID){
        setCurrentRule(ruleID);
        Node node = currentRule.nodes().get(nodeID);
        scene.executing(node);
    }

    @Override
    public void fatalError(String message) throws Exception{
        throw new IOException(message);
    }

    @Override
    public void onSuccessful() throws Exception{}
}

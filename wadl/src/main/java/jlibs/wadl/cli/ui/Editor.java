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

package jlibs.wadl.cli.ui;

import jlibs.core.io.IOUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;

/**
 * @author Santhosh Kumar T
 */
public class Editor extends JFrame{
    private RSyntaxTextArea textArea = new RSyntaxTextArea(30, 100);
    
    public Editor(final File file, String syntax) throws Exception{
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        textArea.setCodeFoldingEnabled(true);
        getContentPane().add(new RTextScrollPane(textArea));
        textArea.setSyntaxEditingStyle(syntax);
        textArea.setText(IOUtil.pump(new FileReader(file), true).toString());
        file.delete();
        pack();
        setLocationRelativeTo(null);

        String actionName = "save-and-quit";
        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK), actionName);
        textArea.getActionMap().put(actionName, new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    IOUtil.pump(new StringReader(textArea.getText()), new FileWriter(file), true, true);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) throws Exception{
        new Editor(new File(args[0]), args[1]).setVisible(true);
    }
}

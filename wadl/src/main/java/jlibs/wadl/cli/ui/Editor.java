/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

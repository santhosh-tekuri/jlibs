package jlibs.wadl;

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

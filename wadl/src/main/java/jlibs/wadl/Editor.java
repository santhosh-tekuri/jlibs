package jlibs.wadl;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;

/**
 * @author Santhosh Kumar T
 */
public class Editor extends JDialog{
    private RSyntaxTextArea textArea = new RSyntaxTextArea();
    
    public Editor(){
        setModal(true);
        getContentPane().add(new RTextScrollPane(textArea));
        pack();
    }
    
    public String show(final String content, final String syntax) throws InterruptedException{
        textArea.setText("");
        textArea.setSyntaxEditingStyle(syntax);
        textArea.setText(content);
        setVisible(true);
        return textArea.getText();
    }
}

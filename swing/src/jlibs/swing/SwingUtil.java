package jlibs.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Santhosh Kumar T
 */
public class SwingUtil{
    public static void setInitialFocus(Window window, final Component comp){
        window.addWindowFocusListener(new WindowAdapter(){
            @Override
            public void windowGainedFocus(WindowEvent we){
                comp.requestFocusInWindow();
                we.getWindow().removeWindowFocusListener(this);
            }
        });
    }

    @SuppressWarnings({"UnusedAssignment"})
    public static void doAction(JTextField textField){
        String command = null;
        if(textField.getAction()!=null)
            command = (String)textField.getAction().getValue(Action.ACTION_COMMAND_KEY);
        ActionEvent event = null;
        
        for(ActionListener listener: textField.getActionListeners()){
            if(event==null)
                event = new ActionEvent(textField, ActionEvent.ACTION_PERFORMED, command, System.currentTimeMillis(), 0);
            listener.actionPerformed(event);
        }
    }
}

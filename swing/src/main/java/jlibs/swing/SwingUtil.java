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

package jlibs.swing;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Various Swing related utilities
 * 
 * @author Santhosh Kumar T
 */
public class SwingUtil{
    /**
     * sets intial focus in window to the specified component
     *
     * @param window    window on which focus has to be set
     * @param comp      component which need to have initial focus
     */
    public static void setInitialFocus(Window window, final Component comp){
        window.addWindowFocusListener(new WindowAdapter(){
            @Override
            public void windowGainedFocus(WindowEvent we){
                comp.requestFocusInWindow();
                we.getWindow().removeWindowFocusListener(this);
            }
        });
    }

    /**
     * Programmatically perform action on textfield.This does the same
     * thing as if the user had pressed enter key in textfield.
     *
     * @param textField textField on which action to be preformed
     */
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

    /**
     * sets text of textComp without moving its caret.
     *
     * @param textComp  text component whose text needs to be set
     * @param text      text to be set. null will be treated as empty string
     */
    public static void setText(JTextComponent textComp, String text){
        if(text==null)
            text = "";
        
        if(textComp.getCaret() instanceof DefaultCaret){
            DefaultCaret caret = (DefaultCaret)textComp.getCaret();
            int updatePolicy = caret.getUpdatePolicy();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            try{
                textComp.setText(text);
            }finally{
                caret.setUpdatePolicy(updatePolicy);
            }
        }else{
            int mark = textComp.getCaret().getMark();
            int dot = textComp.getCaretPosition();
            try{
                textComp.setText(text);
            }finally{
                int len = textComp.getDocument().getLength();
                if(mark>len)
                    mark = len;
                if(dot>len)
                    dot = len;
                textComp.setCaretPosition(mark);
                if(dot!=mark)
                    textComp.moveCaretPosition(dot);
            }
        }
    }
}

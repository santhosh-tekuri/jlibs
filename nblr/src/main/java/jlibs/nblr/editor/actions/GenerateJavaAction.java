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

package jlibs.nblr.editor.actions;

import jlibs.core.annotation.processing.Printer;
import jlibs.nblr.codegen.java.JavaCodeGenerator;
import jlibs.nblr.editor.RuleScene;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;

/**
 * @author Santhosh Kumar T
 */
public class GenerateJavaAction extends AbstractAction{
    private RuleScene scene;
    public GenerateJavaAction(RuleScene scene){
        super("Generate Java...");
        this.scene = scene;
    }

    @Override
    public void actionPerformed(ActionEvent ae){
        Printer printer = new Printer(new PrintWriter(System.out, true));
        JavaCodeGenerator codeGenerator = new JavaCodeGenerator(scene.getSyntax(), printer);
        int response = JOptionPane.showConfirmDialog(scene.getView(), "Generate Debuggable Parser ?");
        if(response==JOptionPane.YES_OPTION)
            codeGenerator.setDebuggable();
        else if(response==JOptionPane.CANCEL_OPTION)
            return;
        codeGenerator.generateCode();
    }
}

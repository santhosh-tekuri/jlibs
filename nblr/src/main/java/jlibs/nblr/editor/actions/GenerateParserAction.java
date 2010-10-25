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
import jlibs.core.io.FileUtil;
import jlibs.core.util.CollectionUtil;
import jlibs.nblr.codegen.java.JavaCodeGenerator;
import jlibs.nblr.editor.RuleScene;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * @author Santhosh Kumar T
 */
public class GenerateParserAction extends AbstractAction{
    private RuleScene scene;
    public GenerateParserAction(RuleScene scene){
        super("Generate Parser...");
        this.scene = scene;
    }

    @Override
    public void actionPerformed(ActionEvent ae){
        JavaCodeGenerator codeGenerator = new JavaCodeGenerator(scene.getSyntax());
        if(scene.file!=null){
            File propsFile = new File(scene.file.getParentFile(), FileUtil.getName(scene.file.getName())+".properties");
            try{
                if(propsFile.exists())
                    CollectionUtil.readProperties(new FileInputStream(propsFile), codeGenerator.properties);
                else
                    codeGenerator.properties.store(new FileOutputStream(propsFile), "Properties for "+scene.file.getName());
            }catch (IOException ex){
                ex.printStackTrace();
                JOptionPane.showMessageDialog(scene.getView(), ex.getMessage());
            }
        }
        
        int response = JOptionPane.showConfirmDialog(scene.getView(), "Generate Debuggable Parser ?");
        if(response==JOptionPane.YES_OPTION)
            codeGenerator.setDebuggable();
        else if(response!=JOptionPane.NO_OPTION)
            return;
        Printer printer = new Printer(new PrintWriter(System.out, true));
        codeGenerator.generateParser(printer);
    }
}

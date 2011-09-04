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
 * @author Sathosh Kumar T
 */
public abstract class GenerateJavaFileAction extends AbstractAction{
    protected RuleScene scene;
    public GenerateJavaFileAction(String name, RuleScene scene){
        super(name);
        this.scene = scene;
    }

    @Override
    public final void actionPerformed(ActionEvent e){
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

        if(!askConfirmation(codeGenerator))
            return;

        File file = codeGenerator.fileProperty(classPropertyName());
        if(scene.file!=null && !file.isAbsolute())
            file = new File(scene.file.getParentFile(), file.getPath());
        file.getParentFile().mkdirs();
        Printer printer = null;
        try{
            OutputStream out = new FileOutputStream(file);
            printer = new Printer(new PrintWriter(out, true));
            generateJavaFile(codeGenerator, printer);
        }catch(IOException ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(scene.getView(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }finally{
            if(printer!=null)
                printer.close();
        }

    }

    protected abstract boolean askConfirmation(JavaCodeGenerator codeGenerator);
    protected abstract String classPropertyName();
    protected abstract void generateJavaFile(JavaCodeGenerator codeGenerator, Printer printer);
}

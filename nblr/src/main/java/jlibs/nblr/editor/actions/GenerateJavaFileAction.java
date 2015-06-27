/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

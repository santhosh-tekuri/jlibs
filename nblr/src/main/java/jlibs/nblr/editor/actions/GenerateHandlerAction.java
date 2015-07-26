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

package jlibs.nblr.editor.actions;

import jlibs.core.annotation.processing.Printer;
import jlibs.nblr.codegen.java.JavaCodeGenerator;
import jlibs.nblr.editor.RuleScene;

import javax.swing.*;

/**
 * @author Santhosh Kumar T
 */
public class GenerateHandlerAction extends GenerateJavaFileAction{
    public GenerateHandlerAction(RuleScene scene){
        super("Generate Consumer...", scene);
    }

    @Override
    protected boolean askConfirmation(JavaCodeGenerator codeGenerator){
        int response = JOptionPane.showConfirmDialog(scene.getView(), "Generate Class ?");
        if(response==JOptionPane.YES_OPTION)
            codeGenerator.properties.put(JavaCodeGenerator.HANDLER_IS_CLASS, "true");
        else if(response!=JOptionPane.NO_OPTION)
            return false;
        return true;
    }

    @Override
    protected String classPropertyName(){
        return JavaCodeGenerator.HANDLER_CLASS_NAME;
    }

    @Override
    protected void generateJavaFile(JavaCodeGenerator codeGenerator, Printer printer){
        codeGenerator.generateConsumer(printer);
    }
}

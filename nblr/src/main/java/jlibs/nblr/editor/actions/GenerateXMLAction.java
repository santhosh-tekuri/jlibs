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

import jlibs.nblr.Syntax;
import jlibs.nblr.editor.RuleScene;
import jlibs.nblr.editor.serialize.SyntaxBinding;
import jlibs.nblr.editor.serialize.SyntaxDocument;
import jlibs.xml.sax.binding.BindingHandler;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.xml.transform.stream.StreamResult;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Santhosh Kumar T
 */
public class GenerateXMLAction extends AbstractAction{
    private RuleScene scene;
    public GenerateXMLAction(RuleScene scene){
        super("Generate XML...");
        this.scene = scene;
    }

    @Override
    public void actionPerformed(ActionEvent ae){
        try{
            StringWriter writer = new StringWriter();
            serialize(scene.getSyntax(), writer);
            String xml1 = writer.toString();
            System.out.println(xml1);
            System.out.println("============================================");

            BindingHandler handler = new BindingHandler(SyntaxBinding.class);
            Syntax newSyntax = (Syntax)handler.parse(new InputSource(new StringReader(xml1)));

            writer = new StringWriter();
            serialize(newSyntax, writer);
            String xml2 = writer.toString();
            System.out.println(xml2);
            System.out.println("============================================");

            System.out.println(xml1.equals(xml2) ? "passed" : "failed");
        }catch(Exception ex){
            ex.printStackTrace();
            JOptionPane.showMessageDialog(scene.getView(), ex.getMessage());
        }
    }

    private void serialize(Syntax syntax, Writer writer) throws Exception{
        SyntaxDocument xml = new SyntaxDocument(new StreamResult(writer));
        xml.startDocument();
        xml.add(syntax);
        xml.endDocument();
    }
}

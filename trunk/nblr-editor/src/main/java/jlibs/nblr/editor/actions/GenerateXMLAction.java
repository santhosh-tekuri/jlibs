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

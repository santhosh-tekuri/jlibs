/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.graph.navigators;

import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class NavigatorTest extends JFrame{
    protected JTree tree = new JTree();

    public NavigatorTest(String title){
        super(title);
        Container contents = getContentPane();
        contents.add(new JScrollPane(tree));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
    }

    public JTree getTree(){
        return tree;
    }

    public static void main(String[] args) throws SAXException, IOException{
        NavigatorTest test = new NavigatorTest("Navigator Test");
        JTree tree = test.getTree();
        test.setVisible(true);
    }
}

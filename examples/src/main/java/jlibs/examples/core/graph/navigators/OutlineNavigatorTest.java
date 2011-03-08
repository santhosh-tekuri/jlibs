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

package jlibs.examples.core.graph.navigators;

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Path;
import jlibs.core.graph.navigators.FilteredTreeNavigator;
import jlibs.core.graph.navigators.PathNavigator;
import jlibs.swing.outline.DefaultColumn;
import jlibs.swing.outline.DefaultRenderDataProvider;
import jlibs.swing.outline.DefaultRowModel;
import jlibs.swing.tree.NavigatorTreeModel;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsd.XSNavigator;
import jlibs.xml.xsd.XSParser;
import jlibs.xml.xsd.XSUtil;
import jlibs.xml.xsd.display.*;
import org.apache.xerces.xs.XSModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RowModel;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class OutlineNavigatorTest extends JFrame{
    protected Outline outline = new Outline();

    public OutlineNavigatorTest(String title){
        super(title);
        Container contents = getContentPane();
        contents.add(new JScrollPane(outline));
        outline.setRootVisible(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
    }

    public Outline getOutline(){
        return outline;
    }

    @SuppressWarnings({"unchecked"})
    public static void main(String[] args) throws Exception{
        String url = JOptionPane.showInputDialog("File/URL", "http://schemas.xmlsoap.org/wsdl/");
        if(url==null)
            return;
        XSModel model = new XSParser().parse(url);
        MyNamespaceSupport nsSupport = XSUtil.createNamespaceSupport(model);

        Navigator navigator1 = new FilteredTreeNavigator(new XSNavigator(), new XSDisplayFilter());
        Navigator navigator = new PathNavigator(navigator1);
        XSPathDiplayFilter filter = new XSPathDiplayFilter(navigator1);
        navigator = new FilteredTreeNavigator(navigator, filter);
        TreeModel treeModel = new NavigatorTreeModel(new Path(model), navigator);
        RowModel rowModel = new DefaultRowModel(new DefaultColumn("Detail", String.class, new XSDisplayValueVisitor(nsSupport))/*, new ClassColumn()*/);
        
        OutlineNavigatorTest test = new OutlineNavigatorTest("Navigator Test");
        Outline outline = test.getOutline();
        outline.setShowGrid(false);
        outline.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        outline.setModel(DefaultOutlineModel.createOutlineModel(treeModel, rowModel));
        outline.getColumnModel().getColumn(1).setMinWidth(150);

        DefaultRenderDataProvider dataProvider = new DefaultRenderDataProvider();
        dataProvider.setDisplayNameVisitor(new XSDisplayNameVisitor(nsSupport, filter));
        dataProvider.setForegroundVisitor(new XSColorVisitor(filter));
        dataProvider.setFontStyleVisitor(new XSFontStyleVisitor(filter));
        outline.setRenderDataProvider(dataProvider);
        
        test.setVisible(true);
    }
}
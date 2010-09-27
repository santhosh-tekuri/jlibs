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

package jlibs.swing.tree;

import jlibs.core.graph.Visitor;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class MyTreeCellRenderer extends JLabel implements TreeCellRenderer{
    protected JTree tree;
    protected boolean selected;
    protected boolean expanded;
    protected boolean leaf;
    protected int row;
    protected boolean hasFocus;
    
    public Color foreground;
    public Color selectionForeground;
    public Color background;
    public Color selectionBackground;

    public MyTreeCellRenderer(){
        setOpaque(true);
        foreground = UIManager.getColor("Tree.textForeground");
        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        background = UIManager.getColor("Tree.textBackground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus){
        this.tree = tree;
        this.selected = selected;
        this.expanded = expanded;
        this.leaf = leaf;
        this.row = row;
        this.hasFocus = hasFocus;

        setFont(tree.getFont());
        setText(getText(value));
        setForeground(selected ? selectionForeground : foreground);
        setBackground(selected ? selectionBackground : background);

        return this;
    }

    private Visitor<Object, String> textConvertor;

    public void setTextConvertor(Visitor<Object, String> textConvertor){
        this.textConvertor = textConvertor;
    }

    public String getText(Object value){
        if(textConvertor!=null)
            return textConvertor.visit(value);
        else
            return tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }
}

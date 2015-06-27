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

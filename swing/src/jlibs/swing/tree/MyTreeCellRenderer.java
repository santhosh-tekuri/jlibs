package jlibs.swing.tree;

import jlibs.graph.Visitor;

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

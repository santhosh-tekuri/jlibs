package jlibs.swing.outline;

import org.netbeans.swing.outline.RenderDataProvider;

import javax.swing.*;
import java.awt.*;

import jlibs.graph.Visitor;
import jlibs.swing.EmptyIcon;

/**
 * @author Santhosh Kumar T
 */
public class DefaultRenderDataProvider implements RenderDataProvider{
    private Visitor<Object, String> displayNameVisitor;

    public void setDisplayNameVisitor(Visitor<Object, String> displayNameVisitor){
        this.displayNameVisitor = displayNameVisitor;
    }

    @Override
    public String getDisplayName(Object obj){
        return displayNameVisitor!=null ? displayNameVisitor.visit(obj) : null;
    }

    @Override
    public boolean isHtmlDisplayName(Object o){
        return false;
    }

    @Override
    public Color getBackground(Object o){
        return null;
    }

    @Override
    public Color getForeground(Object o){
        return null;
    }

    @Override
    public String getTooltipText(Object o){
        return null;
    }

    @Override
    public Icon getIcon(Object o){
        return EmptyIcon.INSTANCE;
    }
}

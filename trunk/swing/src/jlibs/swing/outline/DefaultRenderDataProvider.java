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
    /*-------------------------------------------------[ DisplayName ]---------------------------------------------------*/
    
    private Visitor<Object, String> displayNameVisitor;

    public void setDisplayNameVisitor(Visitor<Object, String> displayNameVisitor){
        this.displayNameVisitor = displayNameVisitor;
    }

    @Override
    public String getDisplayName(Object obj){
        return displayNameVisitor!=null ? displayNameVisitor.visit(obj) : null;
    }

    /*-------------------------------------------------[ Background ]---------------------------------------------------*/
    
    private Visitor<Object, Color> backgroundVisitor;

    public void setBackgroundVisitor(Visitor<Object, Color> backgroundVisitor){
        this.backgroundVisitor = backgroundVisitor;
    }

    @Override
    public Color getBackground(Object obj){
        return backgroundVisitor!=null ? backgroundVisitor.visit(obj) : null;
    }

    /*-------------------------------------------------[ Foreground ]---------------------------------------------------*/

    private Visitor<Object, Color> foregroundVisitor;

    public void setForegroundVisitor(Visitor<Object, Color> foregroundVisitor){
        this.foregroundVisitor = foregroundVisitor;
    }

    @Override
    public Color getForeground(Object obj){
        return foregroundVisitor!=null ? foregroundVisitor.visit(obj) : null;
    }

    /*-------------------------------------------------[ Others ]---------------------------------------------------*/
    
    @Override
    public boolean isHtmlDisplayName(Object o){
        return false;
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

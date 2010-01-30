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

package jlibs.swing.outline;

import org.netbeans.swing.outline.RenderDataProvider;

import javax.swing.*;
import java.awt.*;

import jlibs.core.graph.Visitor;
import jlibs.swing.EmptyIcon;

/**
 * @author Santhosh Kumar T
 */
public class DefaultRenderDataProvider implements RenderDataProvider{
    /*-------------------------------------------------[ DisplayName ]---------------------------------------------------*/
    
    private Visitor<Object, String> displayNameVisitor;
    private Visitor<Object, Integer> fontStyleVisitor;

    public void setDisplayNameVisitor(Visitor<Object, String> displayNameVisitor){
        this.displayNameVisitor = displayNameVisitor;
    }

    public void setFontStyleVisitor(Visitor<Object, Integer> fontStyleVisitor){
        this.fontStyleVisitor = fontStyleVisitor;
    }

    @Override
    public String getDisplayName(Object obj){
        String str = displayNameVisitor != null ? displayNameVisitor.visit(obj) : null;
        if(fontStyleVisitor!=null){
            int style = fontStyleVisitor.visit(obj);
            if((style!=Font.PLAIN)){
                str = str.replace("<", "&lt;");
                if((style&Font.BOLD)!=0)
                    str = "<b>"+str+"</b>";
                if((style&Font.ITALIC)!=0)
                    str = "<i>"+str+"</i>";
                str = "<html><body>"+str+"</body></html>";
            }
        }
        return str;
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

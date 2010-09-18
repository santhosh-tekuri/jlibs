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

package jlibs.nblr.editor.debug;

/**
 * @author Santhosh Kumar T
 */

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class GuardBlockHighlightPainter implements Highlighter.HighlightPainter{
    private Color color;

    public GuardBlockHighlightPainter(Color color){
        this.color = color;
    }

    public GuardBlockHighlightPainter(){
        this(new Color(225, 236, 247));
    }

    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c){
        _paint(g, p0-1, p1+1, bounds, c);
    }

    public void _paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c){
        Rectangle alloc = bounds.getBounds();
        try{
            // --- determine locations ---
            TextUI mapper = c.getUI();
            Rectangle p0 = mapper.modelToView(c, offs0);
            Rectangle p1 = mapper.modelToView(c, offs1);

            // --- render ---

            g.setColor(color);
            if(p0.y==p1.y){
                // same line, render a rectangle
                Rectangle r = p0.union(p1);
                g.fillRect(r.x, r.y, r.width, r.height);
            } else{
                // different lines
                int p0ToMarginWidth = alloc.x+alloc.width-p0.x;
                g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
                if((p0.y+p0.height)!=p1.y){
                    g.fillRect(alloc.x, p0.y+p0.height, alloc.width,
                            p1.y-(p0.y+p0.height));
                }
                g.fillRect(alloc.x, p1.y, (p1.x-alloc.x), p1.height);
            }
        } catch(BadLocationException e){
            // can't render
        }
    }
}

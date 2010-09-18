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

package jlibs.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * @author Santhosh Kumar T
 */
public class VerticalTextIcon implements Icon, SwingConstants{
    private Component comp;
    private String text;
    private boolean clockwize;

    public VerticalTextIcon(Component comp, String text, boolean clockwize){
        this.comp = comp;
        this.text = text;
        this.clockwize = clockwize;
    }

    public void paintIcon(Component c, Graphics g, int x, int y){
        Graphics2D g2 = (Graphics2D)g;
        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        AffineTransform oldTransform = g2.getTransform();

        g.setFont(c.getFont());
        g.setColor(c.getForeground());
        if(clockwize){
            g2.translate(x+getIconWidth(), y);
            g2.rotate(Math.PI/2);
        }else{
            g2.translate(x, y+getIconHeight());
            g2.rotate(-Math.PI/2);
        }
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, 0, fm.getLeading()+fm.getAscent());

        g.setFont(oldFont);
        g.setColor(oldColor);
        g2.setTransform(oldTransform);
    }

    public int getIconWidth(){
        return comp.getGraphics().getFontMetrics().getHeight();
    }

    public int getIconHeight(){
        return SwingUtilities.computeStringWidth(comp.getGraphics().getFontMetrics(), text);
    }
}
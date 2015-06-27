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
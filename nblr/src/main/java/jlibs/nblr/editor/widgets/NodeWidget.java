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

package jlibs.nblr.editor.widgets;

import jlibs.nblr.editor.Util;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class NodeWidget extends Widget implements NBLRWidget{
    private static Color COLOR_FOREGROUND = Color.BLACK;
    private static Color COLOR_BACKGROUND = Color.WHITE;

    public NodeWidget(Scene scene){
        super(scene);
    }

    private static final int radius = 10;
    private static final int vborder = 1;

    @Override
    @SuppressWarnings({"PointlessArithmeticExpression"})    
    protected Rectangle calculateClientArea(){
        String text = Util.node(this).toString();

        int w, h;
        if(text.length()==0)
            w = h = 2*radius;
        else{
            JLabel label = new JLabel(text);
            label.setFont(getFont());
            Dimension dim = label.getPreferredSize();
            w = dim.width + dim.height;
            h = dim.height + 2*vborder;
        }
        return new Rectangle(-w/2, -h/2, w+1, h+1);
    }

    @Override
    protected void paintWidget(){
        Graphics2D g = getGraphics();
        g.setStroke(Util.STROKE_2);
        g.setFont(getFont());
        Rectangle bounds = getBounds();

        String text = Util.node(this).toString();
        if(text.length()==0){
            g.setColor((Color)getBackground());
            g.fillOval(bounds.x, bounds.y, bounds.width-1, bounds.height-1);
            g.setColor(getForeground());
            g.drawOval(bounds.x, bounds.y, bounds.width-1, bounds.height-1);
        }else{
            g.setColor((Color)getBackground());
            int arc = (bounds.height-1);
            g.fillRoundRect(bounds.x, bounds.y, bounds.width-1, bounds.height-1, arc, arc);
            g.setColor(getForeground());
            g.drawString(text, bounds.x+(bounds.height-1)/2, bounds.y+bounds.height-vborder-1-g.getFontMetrics().getMaxDescent());
            g.drawRoundRect(bounds.x, bounds.y, bounds.width-1, bounds.height-1, arc, arc);
        }
    }

    @Override
    public void highLight(boolean doHighLight){
        Color fg = COLOR_FOREGROUND;
        Color bg;
        if(doHighLight)
            bg = COLOR_HILIGHT;
        else
            bg = executing ? COLOR_DEBUGGER : COLOR_BACKGROUND;

        setForeground(fg);
        setBackground(bg);
    }

    private boolean executing = false;
    @Override
    public void executing(boolean executing){
        this.executing = executing;
        highLight(false);
    }
}

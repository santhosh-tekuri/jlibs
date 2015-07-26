/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

package jlibs.nblr.editor.debug;

/**
 * @author Santhosh Kumar T
 */

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class NewLineHighlightPainter implements Highlighter.HighlightPainter{
    private Color color;

    public NewLineHighlightPainter(Color color){
        this.color = color;
    }

    public NewLineHighlightPainter(){
        this(new Color(225, 236, 247));
    }

    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c){
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

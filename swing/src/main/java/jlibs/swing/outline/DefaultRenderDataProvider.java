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

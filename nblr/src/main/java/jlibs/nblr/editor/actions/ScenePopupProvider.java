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

package jlibs.nblr.editor.actions;

import jlibs.nblr.editor.RuleScene;
import jlibs.nblr.editor.UsagesDialog;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Santhosh Kumar T
 */
public class ScenePopupProvider implements PopupMenuProvider{
    private RuleScene scene;

    public ScenePopupProvider(RuleScene scene){
        this.scene = scene;
    }

    public JPopupMenu getPopupMenu(Widget widget, Point localLocation){
        JPopupMenu popup = new JPopupMenu();
        popup.add(new GenerateParserAction(scene));
        popup.add(new GenerateHandlerAction(scene));
        popup.add(new GenerateXMLAction(scene));
        popup.add(new AbstractAction("Usages..."){
            @Override
            public void actionPerformed(ActionEvent ae){
                new UsagesDialog(SwingUtilities.getWindowAncestor(scene.getView()), scene).setVisible(true);
            }
        });

        return popup;
    }
}


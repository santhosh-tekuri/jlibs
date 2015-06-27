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

package jlibs.nblr.editor.actions;

import jlibs.nblr.editor.widgets.NBLRWidget;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author Santhosh Kumar T
 */
public class Highlighter implements TwoStateHoverProvider{
    private TwoStateHoverProvider delegate;
    public Highlighter(TwoStateHoverProvider delegate){
        this.delegate = delegate;
    }

    private Widget resolve(Widget widget){
        ObjectScene scene = (ObjectScene)widget.getScene();
        return scene.findWidget(scene.findObject(widget));
    }

    public void unsetHovering(Widget widget){
        widget = resolve(widget);
        if(widget==null)
            return;
        ((NBLRWidget)widget).highLight(false);
        if(delegate!=null)
            delegate.unsetHovering(widget);
    }

    public void setHovering(Widget widget){
        widget = resolve(widget);
        if(widget==null)
            return;
        ((NBLRWidget)widget).highLight(true);

        if(delegate!=null)
            delegate.setHovering(widget);
    }
}

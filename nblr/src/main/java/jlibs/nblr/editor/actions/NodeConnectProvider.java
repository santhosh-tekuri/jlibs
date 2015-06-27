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

import jlibs.nblr.editor.RuleScene;
import jlibs.nblr.rules.Node;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

import static jlibs.nblr.editor.Util.model;
import static jlibs.nblr.editor.Util.node;

/**
 * @author Santhosh Kumar T
 */
public class NodeConnectProvider implements ConnectProvider{
    public boolean isSourceWidget(Widget sourceWidget){
        return model(sourceWidget) instanceof Node;
    }

    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget){
        return model(targetWidget) instanceof Node ? ConnectorState.ACCEPT : ConnectorState.REJECT;
    }

    public boolean hasCustomTargetWidgetResolver(Scene scene){
        return false;
    }

    public Widget resolveTargetWidget (Scene scene, Point sceneLocation){
        return null;
    }

    public void createConnection(Widget sourceWidget, Widget targetWidget){
        node(sourceWidget).addEdgeTo(node(targetWidget));
        ((RuleScene)sourceWidget.getScene()).refresh();
    }
}

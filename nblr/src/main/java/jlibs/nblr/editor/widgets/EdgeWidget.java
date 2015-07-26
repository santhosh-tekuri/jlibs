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

package jlibs.nblr.editor.widgets;

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;

/**
 * @author Santhosh Kumar T
 */
public class EdgeWidget extends ConnectionWidget implements NBLRWidget{
    private static Color COLOR = new Color(0, 0, 128);

    public EdgeWidget(Scene scene){
        super(scene);
    }

    @Override
    public void highLight(boolean doHighLight){
        Color color;
        if(doHighLight)
            color = COLOR_HILIGHT;
        else
            color = executing ? COLOR_DEBUGGER : COLOR;

        setLineColor(color);
        getChildren().get(0).setForeground(color);
    }

    private boolean executing = false;
    @Override
    public void executing(boolean executing){
        this.executing = executing;
        highLight(false);
    }
}

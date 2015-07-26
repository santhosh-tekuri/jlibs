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

package jlibs.swing.xml.xsd;

import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RowModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.apache.xerces.xs.XSModel;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;

import jlibs.core.graph.Navigator;
import jlibs.core.graph.Path;
import jlibs.core.graph.navigators.FilteredTreeNavigator;
import jlibs.core.graph.navigators.PathNavigator;
import jlibs.swing.tree.NavigatorTreeModel;
import jlibs.swing.outline.DefaultRowModel;
import jlibs.swing.outline.DefaultColumn;
import jlibs.swing.outline.DefaultRenderDataProvider;
import jlibs.xml.sax.helpers.MyNamespaceSupport;
import jlibs.xml.xsd.XSUtil;
import jlibs.xml.xsd.XSNavigator;
import jlibs.xml.xsd.display.*;

/**
 * @author Santhosh Kumar T
 */
public class XSDOutlinePanel extends JPanel{
    protected Outline outline = new Outline();
    
    public XSDOutlinePanel(){
        super(new BorderLayout());

        add(new JScrollPane(outline), BorderLayout.CENTER);
        outline.setRootVisible(false);
        outline.setShowGrid(false);
        outline.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }

    public void setXSModel(XSModel model){
        setXSModel(model, XSUtil.createNamespaceSupport(model));
    }

    @SuppressWarnings({"unchecked"})
    public void setXSModel(XSModel model, MyNamespaceSupport nsSupport){
        Navigator navigator1 = new FilteredTreeNavigator(new XSNavigator(), new XSDisplayFilter());
        Navigator navigator = new PathNavigator(navigator1);
        XSPathDiplayFilter filter = new XSPathDiplayFilter(navigator1);
        navigator = new FilteredTreeNavigator(navigator, filter);
        
        TreeModel treeModel = new NavigatorTreeModel(new Path(model), navigator);
        RowModel rowModel = new DefaultRowModel(new DefaultColumn("Detail", String.class, new XSDisplayValueVisitor(nsSupport))/*, new ClassColumn()*/);

        outline.setModel(DefaultOutlineModel.createOutlineModel(treeModel, rowModel));
        outline.getColumnModel().getColumn(1).setMinWidth(150);

        DefaultRenderDataProvider dataProvider = new DefaultRenderDataProvider();
        dataProvider.setDisplayNameVisitor(new XSDisplayNameVisitor(nsSupport, filter));
        dataProvider.setForegroundVisitor(new XSColorVisitor(filter));
        dataProvider.setFontStyleVisitor(new XSFontStyleVisitor(filter));
        outline.setRenderDataProvider(dataProvider);
    }
}

package jlibs.graph.navigators;

import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RowModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.xml.sax.SAXException;
import org.apache.xerces.xs.XSModel;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.io.IOException;

import jlibs.xml.xsd.*;
import jlibs.xml.xsd.display.XSDisplayFilter;
import jlibs.xml.xsd.display.XSDisplayNameVisitor;
import jlibs.xml.xsd.display.XSDisplayValueVisitor;
import jlibs.xml.xsd.display.XSPathDiplayFilter;
import jlibs.xml.xsd.display.XSColorVisitor;
import jlibs.xml.sax.MyNamespaceSupport;
import jlibs.graph.Navigator;
import jlibs.graph.Path;
import jlibs.swing.tree.NavigatorTreeModel;
import jlibs.swing.outline.DefaultRowModel;
import jlibs.swing.outline.ClassColumn;
import jlibs.swing.outline.DefaultRenderDataProvider;
import jlibs.swing.outline.DefaultColumn;

/**
 * @author Santhosh Kumar T
 */
public class OutlineNavigatorTest extends JFrame{
    protected Outline outline = new Outline();

    public OutlineNavigatorTest(String title){
        super(title);
        Container contents = getContentPane();
        contents.add(new JScrollPane(outline));
        outline.setRootVisible(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
    }

    public Outline getOutline(){
        return outline;
    }

    public static void main(String[] args) throws SAXException, IOException{
//        XSModel model = new XSParser().parse("xml/xsds/note.xsd");
        XSModel model = new XSParser().parse("/Users/santhosh/Sonoa/Workspaces/SVN/schemas_3050/sci/application.xsd");
        MyNamespaceSupport nsSupport = XSUtil.createNamespaceSupport(model);

        Navigator navigator1 = new FilteredTreeNavigator(new XSNavigator(), new XSDisplayFilter());
        Navigator navigator = new PathNavigator(navigator1);
        XSPathDiplayFilter filter = new XSPathDiplayFilter(navigator1);
        navigator = new FilteredTreeNavigator(navigator, filter);
        TreeModel treeModel = new NavigatorTreeModel(new Path(model), navigator);
        RowModel rowModel = new DefaultRowModel(new DefaultColumn("Detail", String.class, new XSDisplayValueVisitor(nsSupport)), new ClassColumn());
        
        OutlineNavigatorTest test = new OutlineNavigatorTest("Navigator Test");
        Outline outline = test.getOutline();
        outline.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        outline.setModel(DefaultOutlineModel.createOutlineModel(treeModel, rowModel));

        DefaultRenderDataProvider dataProvider = new DefaultRenderDataProvider();
        dataProvider.setDisplayNameVisitor(new XSDisplayNameVisitor(nsSupport, filter));
        dataProvider.setForegroundVisitor(new XSColorVisitor(filter));
        outline.setRenderDataProvider(dataProvider);
        
        test.setVisible(true);
    }
}
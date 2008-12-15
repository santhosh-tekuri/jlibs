package jlibs.graph.navigators;

import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class NavigatorTest extends JFrame{
    protected JTree tree = new JTree();

    public NavigatorTest(String title){
        super(title);
        Container contents = getContentPane();
        contents.add(new JScrollPane(tree));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
    }

    public JTree getTree(){
        return tree;
    }

    public static void main(String[] args) throws SAXException, IOException{
        NavigatorTest test = new NavigatorTest("Navigator Test");
        JTree tree = test.getTree();
        test.setVisible(true);
    }
}

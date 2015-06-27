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

package jlibs.examples.core.graph.navigators;

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

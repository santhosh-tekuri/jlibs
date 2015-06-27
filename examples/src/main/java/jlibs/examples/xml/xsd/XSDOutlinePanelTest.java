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

package jlibs.examples.xml.xsd;

import jlibs.swing.SwingUtil;
import jlibs.swing.xml.xsd.XSDOutlinePanel;
import jlibs.xml.xsd.XSParser;
import org.apache.xerces.xs.XSModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Santhosh Kumar T
 */
public class XSDOutlinePanelTest extends JFrame{
    private XSDOutlinePanel xsdOutline;

    public XSDOutlinePanelTest(){
        super("XSD Viewer");
        JPanel contents = (JPanel)getContentPane();
        contents.setLayout(new BorderLayout(5, 5));
        contents.setBorder(new EmptyBorder(10, 10, 10, 10));
        contents.add(xsdOutline=new XSDOutlinePanel());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("File/URL"), BorderLayout.WEST);
        final JTextField uriText = new JTextField();
        panel.add(uriText);
        panel.add(new JButton(new AbstractAction("Browse..."){
            @Override
            public void actionPerformed(ActionEvent ae){
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("XMLSchema Files", "xsd"));
                if(chooser.showOpenDialog(XSDOutlinePanelTest.this)==JFileChooser.APPROVE_OPTION){
                    uriText.setText(chooser.getSelectedFile().toString());
                    SwingUtil.doAction(uriText);
                }
            }
        }), BorderLayout.EAST);

        uriText.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
                JTextField textField = (JTextField)ae.getSource();
                XSModel model = new XSParser().parse(textField.getText());
                if(model==null)
                    JOptionPane.showMessageDialog(textField, "couldn't load given xsd");
                else
                    xsdOutline.setXSModel(model);
            }
        });
        contents.add(panel, BorderLayout.SOUTH);
        SwingUtil.setInitialFocus(this, uriText);
    }

    public static void main(String[] args) throws Exception{
        SwingUtilities.invokeAndWait(new Runnable(){
            @Override
            public void run(){
                try{
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }catch(Exception ex){
                    // ignore
                }
                XSDOutlinePanelTest frame = new XSDOutlinePanelTest();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(700, 500);
                frame.setVisible(true);
            }
        });
    }
}

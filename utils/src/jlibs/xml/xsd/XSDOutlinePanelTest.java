/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.xsd;

import jlibs.swing.xml.xsd.XSDOutlinePanel;
import jlibs.swing.SwingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.xerces.xs.XSModel;

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

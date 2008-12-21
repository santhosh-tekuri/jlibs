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
        JTextField textField = new JTextField();
        panel.add(textField);
        panel.add(new JButton(new AbstractAction("Browse..."){
            @Override
            public void actionPerformed(ActionEvent ae){
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("XMLSchema Files", "xsd"));
                chooser.showOpenDialog(XSDOutlinePanelTest.this);
            }
        }), BorderLayout.EAST);

        textField.addActionListener(new ActionListener(){
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
        SwingUtil.setInitialFocus(this, textField);
    }

    public static void main(String[] args){
        XSDOutlinePanelTest frame = new XSDOutlinePanelTest();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setVisible(true);
    }
}

package jlibs.swing.event;

import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Santhosh Kumar T
 */
public abstract class UIListener implements
        DocumentListener, ItemListener,
        ListDataListener, ListSelectionListener,
        TableModelListener,
        TreeModelListener, TreeSelectionListener{

    public abstract void changed();

    /*-------------------------------------------------[ DocumentListener ]---------------------------------------------------*/

    @Override
    public void insertUpdate(DocumentEvent de){
        changed();
    }

    @Override
    public void removeUpdate(DocumentEvent de){
        changed();
    }

    @Override
    public void changedUpdate(DocumentEvent de){
        changed();
    }

    /*-------------------------------------------------[ ItemListener ]---------------------------------------------------*/

    @Override
    public void itemStateChanged(ItemEvent ie){
        changed();
    }

    /*-------------------------------------------------[ ListDataListener ]---------------------------------------------------*/
    @Override
    public void intervalAdded(ListDataEvent lde){
        changed();
    }

    @Override
    public void intervalRemoved(ListDataEvent lde){
        changed();
    }

    @Override
    public void contentsChanged(ListDataEvent lde){
        changed();
    }

    /*-------------------------------------------------[ ListSelectionListener ]---------------------------------------------------*/

    @Override
    public void valueChanged(ListSelectionEvent lse){
        changed();
    }

    /*-------------------------------------------------[ TableModelListener ]---------------------------------------------------*/

    @Override
    public void tableChanged(TableModelEvent tme){
        changed();
    }

    /*-------------------------------------------------[ TreeModelListener ]---------------------------------------------------*/

    @Override
    public void treeNodesChanged(TreeModelEvent tme){
        changed();
    }

    @Override
    public void treeNodesInserted(TreeModelEvent tme){
        changed();
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent tme){
        changed();
    }

    @Override
    public void treeStructureChanged(TreeModelEvent tme){
        changed();
    }

    /*-------------------------------------------------[ TreeSelectionListener ]---------------------------------------------------*/

    @Override
    public void valueChanged(TreeSelectionEvent tse){
        changed();
    }

    /*-------------------------------------------------[ ListenTo ]---------------------------------------------------*/

    public void listenTo(JTextComponent textComp){
        textComp.getDocument().addDocumentListener(this);
    }

    public void listenTo(AbstractButton button){
        button.addItemListener(this);
    }

    public void listenTo(JList list, boolean model, boolean selection){
        if(model)
            list.getModel().addListDataListener(this);
        if(selection)
            list.addListSelectionListener(this);
    }

    public void listenTo(JTable table, boolean model, boolean selection){
        if(model)
            table.getModel().addTableModelListener(this);
        if(selection)
            table.getSelectionModel().addListSelectionListener(this);
    }
    
    public void listenTo(JTree tree, boolean model, boolean selection){
        if(model)
            tree.getModel().addTreeModelListener(this);
        if(selection)
            tree.addTreeSelectionListener(this);
    }
}

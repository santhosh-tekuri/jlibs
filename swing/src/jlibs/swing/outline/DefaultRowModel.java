package jlibs.swing.outline;

import org.netbeans.swing.outline.RowModel;

/**
 * @author Santhosh Kumar T
 */
public class DefaultRowModel implements RowModel{
    private Column columns[];

    public DefaultRowModel(Column... columns){
        this.columns = columns;
    }

    @Override
    public int getColumnCount(){
        return columns.length;
    }

    @Override
    public Object getValueFor(Object obj, int i){
        return columns[i].getValueFor(obj);
    }

    @Override
    public Class getColumnClass(int i){
        return columns[i].getColumnClass();
    }

    @Override
    public boolean isCellEditable(Object obj, int i){
        return columns[i].isCellEditable(obj);
    }

    @Override
    public void setValueFor(Object obj, int i, Object value){
        columns[i].setValueFor(obj, value);
    }

    @Override
    public String getColumnName(int i){
        return columns[i].getColumnName();
    }
}

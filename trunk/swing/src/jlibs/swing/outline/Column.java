package jlibs.swing.outline;

/**
 * @author Santhosh Kumar T
 */
public interface Column{
    public String getColumnName();
    public Class getColumnClass();
    public Object getValueFor(Object obj);
    public boolean isCellEditable(Object obj);
    public void setValueFor(Object obj, Object value);
}

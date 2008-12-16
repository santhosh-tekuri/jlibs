package jlibs.swing.outline;

import jlibs.core.graph.Visitor;

/**
 * @author Santhosh Kumar T
 */
public class DefaultColumn implements Column{
    private String name;
    private Class clazz;
    private Visitor visitor;

    public DefaultColumn(String name, Class clazz, Visitor visitor){
        this.name = name;
        this.clazz = clazz;
        this.visitor = visitor;
    }

    @Override
    public String getColumnName(){
        return name;
    }

    @Override
    public Class getColumnClass(){
        return clazz;
    }

    @Override    
    @SuppressWarnings({"unchecked"})
    public Object getValueFor(Object obj){
        return visitor.visit(obj);
    }

    @Override
    public boolean isCellEditable(Object obj){
        return false;
    }

    @Override
    public void setValueFor(Object obj, Object value){
        throw new UnsupportedOperationException();
    }
}

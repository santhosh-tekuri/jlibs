package jlibs.swing.outline;

import jlibs.graph.Path;

/**
 * @author Santhosh Kumar T
 */
public class ClassColumn extends DefaultColumn{
    public ClassColumn(){
        super("Class", Class.class, null);
    }

    @Override
    public Object getValueFor(Object obj){
        if(obj instanceof Path)
            obj = ((Path)obj).getElement();
        return obj.getClass().getSimpleName();
    }
}

package jlibs.swing.outline;

/**
 * @author Santhosh Kumar T
 */
public class ClassColumn extends DefaultColumn{
    public ClassColumn(){
        super("Class", Class.class, null);
    }

    @Override
    public Object getValueFor(Object obj){
        return obj.getClass().getSimpleName();
    }
}

package jlibs.swing;

import jlibs.core.lang.ThreadTasker;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Santhosh Kumar T
 */
public class EDT extends ThreadTasker{
    public static final EDT INSTANCE = new EDT();
    private EDT(){}
    
    @Override
    public boolean isValid(){
        return EventQueue.isDispatchThread();
    }

    @Override
    protected void executeAndWait(Runnable runnable){
        try{
            EventQueue.invokeAndWait(runnable);
        }catch(InterruptedException ex){
            throw new RuntimeException(ex);
        } catch(InvocationTargetException ex){
            throw new RuntimeException(ex.getCause()!=null ? ex.getCause() : ex);
        }
    }

    @Override
    public void executeLater(Runnable runnable){
        EventQueue.invokeLater(runnable);
    }
}

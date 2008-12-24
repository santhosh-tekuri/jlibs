package jlibs.core.lang;

import java.lang.ref.WeakReference;

/**
 * @author Santhosh Kumar T
 */
public class JavaUtil{
    /**
     * This method guarantees that garbage collection is
     * done unlike <code>{@link System#gc()}</code>
     * 
     */
    @SuppressWarnings({"UnusedAssignment"})
    public static void gc(){
        Object obj = new Object();
        WeakReference ref = new WeakReference<Object>(obj);
        obj = null;
        while(ref.get()!=null)
            System.gc();
    }

    public static void main(String[] args){
        gc();
    }
}

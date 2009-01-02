package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public abstract class Task<R> extends ThrowableTask<R, RuntimeException>{
    public Task(){
        super(RuntimeException.class);
    }
}

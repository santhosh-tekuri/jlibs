package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public abstract class ThreadTasker{
    public abstract boolean isValid();
    protected abstract void executeAndWait(Runnable runnable);
    public abstract void executeLater(Runnable runnable);

    public void execute(Runnable runnable){
        if(isValid())
            runnable.run();
        else
            executeAndWait(runnable);
    }

    public <R, E extends Exception> R execute(ThrowableTask<R, E> task) throws E{
        execute(task.asRunnable());
        return task.getResult();
    }
    
    public <R> R execute(Task<R> task){
        execute(task.asRunnable());
        return task.getResult();
    }
}

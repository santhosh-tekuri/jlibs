package jlibs.core.lang.ref;

import java.lang.ref.WeakReference;

/**
 * @author Santhosh Kumar T
 */
public abstract class ActiveWeakReference<T> extends WeakReference<T> implements Runnable{
    @SuppressWarnings({"unchecked"})
    public ActiveWeakReference(T referent){
        super(referent, ActiveReferenceQueue.INSTANCE);
    }
}

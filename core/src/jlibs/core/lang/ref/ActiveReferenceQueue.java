package jlibs.core.lang.ref;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

/**
 * @author Santhosh Kumar T
 */
public class ActiveReferenceQueue extends ReferenceQueue implements Runnable{
    public static final ActiveReferenceQueue INSTANCE = new ActiveReferenceQueue();

    private ActiveReferenceQueue(){
        Thread thread = new Thread(this, getClass().getSimpleName());
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    @SuppressWarnings({"InfiniteLoopStatement"})
    public void run(){
        while(true){
            try{
                Reference ref = super.remove();
                try{
                    if(ref instanceof Runnable)
                        ((Runnable)ref).run();
                }catch(ThreadDeath td){
                    throw td;
                }catch(Throwable thr){
                    thr.printStackTrace();
                }
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
        }
    }

    public void trackGC(Object obj){
        trackGC(obj, null);
    }
    
    public void trackGC(Object obj, String message){
        if(message==null)
            message = obj.getClass().getName()+'@'+System.identityHashCode(obj);
        new TrackableWeakReference(obj, message);
    }

    private static class TrackableWeakReference extends ActiveWeakReference{
        private String message;

        @SuppressWarnings({"unchecked"})
        private TrackableWeakReference(Object referent, String message){
            super(referent);
            this.message = message;
        }

        @Override
        public void run(){
            System.out.println("ActiveReferenceQueue: '"+message+"' has been garbage collected.");
        }
    }
}

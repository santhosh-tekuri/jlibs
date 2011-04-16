/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.lang.ref;

import jlibs.core.lang.ImpossibleException;

import java.lang.ref.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides guaranteed finalization for java objects.
 * <p>
 * This is singleton class and its instance can be accessed as {@code Finalizer.}{@link #INSTANCE}
 * <p>
 * you can tell {@code Finalizer} to execute given {@code runnable} when a particular
 * object gets garbage collected:
 * <pre class="prettyprint">
 * Object myObject = ...;
 * Finalizer.INSTANCE.{@link #track(Object, Runnable) track}(myObject, new Runnable(){
 *    public void run(){
 *        System.out.println("myObject is garbage collected");
 *    }
 * });
 * </pre>
 *
 * {@code Finalizer} creates a {@link java.lang.ref.WeakReference} for the object that needs to
 * be tracked. you can also tell which type of {@link java.lang.ref.Reference} to use as below:
 *
 * <pre class="prettyprint">
 * Object myObject = ...;
 * Finalizer.INSTANCE.{@link #track(Object, Class, Runnable) track}(myObject, {@link java.lang.ref.SoftReference}.class, new Runnable(){
 *    public void run(){
 *        System.out.println("myObject is garbage collected");
 *    }
 * });
 * </pre>
 *
 * All the {@code track(...)} methods return the {@link java.lang.ref.Reference} object created.
 * <p>
 * You can also use {@code Finalizer} as a simple profiler.
 * <pre class="prettyprint">
 * Object myObject = ...;
 * Finalizer.INSTANCE.{@link #trackGC(Object, String) trackGC}(myObject, "myObject is garbage collected");
 * </pre>
 * the above code says to print {@code "myObject is garbage collected"} to console, when {@code myObject} gets
 * garbage collected.
 * <p>
 * This class starts a {@link Thread} named {@code "JLibsFinalizer} with {@link Thread#MIN_PRIORITY}
 *
 * @author Santhosh Kumar T
 */
public class Finalizer extends ReferenceQueue implements Runnable{
    public static final Finalizer INSTANCE = new Finalizer();

    private Finalizer(){
        Thread thread = new Thread(this, "JLibsFinalizer");
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
                Runnable runnable = tracked.remove(ref);
                try{
                    if(runnable!=null)
                        runnable.run();
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

    private Map<Reference, Runnable> tracked = new ConcurrentHashMap<Reference, Runnable>();

    /**
     * tracks the given {@code obj} using {@link java.lang.ref.WeakReference}.<br>
     * Specified {@code runnable} is executed when {@code obj} gets garbage collected.
     *
     * @param obj       object needs to be tracked
     * @param runnable  runnable to be executed when {@code obj} gets garbage collected
     *
     * @return the {@link java.lang.ref.WeakReference} created for {@code obj}
     *
     * @see #track(Object, Class, Runnable)
     */
    @SuppressWarnings("unchecked")
    public <T> WeakReference<T> track(T obj, Runnable runnable){
        return (WeakReference<T>)track(obj, WeakReference.class, runnable);
    }

    /**
     * tracks the given {@code obj} using specified reference {@code type}.<br>
     * Specified {@code runnable} is executed when {@code obj} gets garbage collected.
     *
     * @param obj       object needs to be tracked
     * @param type      type of reference to be used for tracking
     * @param runnable  runnable to be executed when {@code obj} gets garbage collected
     *
     * @return the {@link java.lang.ref.Reference} created for {@code obj}
     *
     * @see #track(Object, Runnable)
     */
    @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
    public <T, R extends Reference<T>> R track(T obj, Class<R> type, Runnable runnable){
        Class clazz = type;
        Reference ref;
        if(clazz==WeakReference.class)
            ref = new WeakReference(obj, this);
        else if(clazz==SoftReference.class)
            ref = new SoftReference(obj, this);
        else if(clazz==PhantomReference.class)
            ref = new PhantomReference(obj, this);
        else
            throw new ImpossibleException();

        tracked.put(ref, runnable);
        return (R)ref;
    }

    /**
     * Prints message to {@code System.out} when {@code obj} gets garbage collected.<p>
     * The message will be {@code obj.getClass().getName()+'@'+System.identityHashCode(obj)}
     *
     * @param obj       object needs to be tracked
     *
     * @see #trackGC(Object, String)
     */
    public void trackGC(Object obj){
        trackGC(obj, null);
    }

    /**
     * Prints {@code message} to {@code System.out} when {@code obj} gets garbage collected
     *
     * @param obj       object needs to be tracked
     * @param message   message to be printed when {@code obj} gets garbage collected.
     *                  if null, the message will be {@code obj.getClass().getName()+'@'+System.identityHashCode(obj)} 
     */
    public void trackGC(Object obj, String message){
        if(message==null)
            message = obj.getClass().getName()+'@'+System.identityHashCode(obj);
        track(obj, new MessagePrinter(message));
    }

    private static class MessagePrinter implements Runnable{
        private String message;

        private MessagePrinter(String message){
            this.message = message;
        }

        @Override
        public void run(){
            System.out.println("JLibsFinalizer: '"+message+"' got garbage collected.");
        }
    }
}

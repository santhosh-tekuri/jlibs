---
title: GarbageCollection
layout: default
---

# Garbage Collection & Finalization #

## Force GC ##

~~~java
System.gc()
~~~

This method suggests JVM to make best effort to invoke garbage collection  
But it doesn't gaurantee that garbage collection is done;

~~~java
import jlibs.core.lang.RuntimeUtil;

RuntimeUtil.gc();
~~~

This method gaurantees that garbage collection is done (unline `System.gc()`)

~~~java
RuntimeUtil.gc(10);
~~~

This method gaurantees that garbage collection is done atleast 10 times

JVM doesn't guarantee that GC is called on JVM exit; To gaurantee use:

~~~java
RuntimeUtil.gcOnExit();
~~~

## Finalization ##

JVM doesn't guarantee that <code>Object.finalize()</code> is always called when an object is claimed for GC.
For more details see [this](http://java.sun.com/developer/technicalArticles/javase/finalization/)

Moreover `finalize()` is somewhat messy. In finalize what if we pass self reference to some other
object. Then there is a chance that, after `finalize()` is called, the object is reachable
and thus cannot be GCed. In future when GC finds that the same object is no longer reachable,
it will call `finalize()<` method again. Thus there is a chance that `finalize()` could be called
more than once for a given object.

We have `jlibs.core.lang.ref.Finalizer` to perform better finalization.

for example see following method in `Finalizer`:

~~~java
public <T> WeakReference<T> track(T obj, Runnable runnable)
~~~

this method ensures that given `runnable` after `obj` has been garbage collected.

See following example usecase:

~~~java
public class MyImage{
    private int nativeImgHandle;
    private Point pos;
    private Dimension dim;

    public MyImage(String path){
        nativeImgHandle = createNativeImageHandle(path);
        Finalizer.INSTANCE.track(this, new MyImageDisposer(nativeImgHandle));
    }

    private int createNativeImageHandle(String path){
        return 10;
    }
}

class MyImageDisposer implements Runnable{
    private int nativeImgHandle;

    public MyImageDisposer(int nativeImgHandle){
        this.nativeImgHandle = nativeImgHandle;
    }

    @Override
    public void run(){
        System.out.println("releasing native image handle");
        //OS.releaseHandle(nativeImgHandle);
    }
}
~~~

Here `MyImage` creates a `nativeImageHandle` which needs to be released when it is garbage collected.

To test that Finalizer is really doing its work:

~~~java
public static void main(String[] args){
    RuntimeUtil.gcOnExit();
    MyImage img = new MyImage("test.gif");
    img = null;
    System.out.println("exiting jvm");
}
~~~

running this will print:

~~~
exiting jvm
releasing native image handle
~~~

If you comment the line `RuntimeUtil.gcOnExit();` in main method,
then you will notice that `releasing native image handle` is not printed.  
This shows that JVM might not do GC on JVM exit.

There is also another variation of track method, for example:

~~~java
Finalizer.INSTANCE.track(this, SoftReference.class, new MyImageDisposer(nativeImgHandle));
~~~

The second argument tells what type of reference to be created. The earlier `track(...)` method
creates WeakReference.

These `track(...)` methods return the `java.lang.ref.Reference` object created.

## Simple Profiling ##

When you are working on a big project, at some point of time you will face memory issues.
Then normally people start using profilers to detect memory leak. The problem with this, by
that time the amount of code in project might be quite big, to analyze when, where and why memory leaked.

Rather than profiling at later time, I suggest to profile your app frequently, when you think
that the code written today might have possible memory leak.

Instead of using full fledged profiler, I normally prefer the following way:

Let us think of scenario. When user open a file in your application, you parse the file into
some sort of java objects and show it to user using some UI. when user close that UI, it is
quite obvious that the java objects which are created to hold the data of file should be eligible
for garbage collection. To check whether your code has memory leak, you can do:

~~~java
public MyCompany parse(File file){
    MyCompany company = new MyCompany();
    // parse file and fill data into company;
    Finalizer.INSTANCE.trackGC(company, "MyCompany["+file+"]");
}

public void onEditorClose(){
    // close the editor
    RuntimeUtil.gc(10);
}
~~~

now launch your application, open a file, do some ui actions and then close the ui.  
if you see the following line in your console:

~~~
JLibsFinalizer: 'MyCompany[C:\sample\test.xml]' got garbage collected.
~~~

you know that your code has no memory leak. If it doesn't print this line, then try again
after changing `RuntimeUtil.gc(10);` to `RuntimeUtil.gc(20);`.

If still you don't get the line printed on console, pick some full-fledged profiler and fix memory
leak.(don't post pone this)

The second argument to <code>trackGC(...)</code> is a readable message that need to be printed when given object is GCed;

There is also another variation of `trackGC(....)` with single argument:

~~~java
public void trackGC(Object obj)<
~~~

this method uses `classname@identityhascode` (for example `MyCompany@13243`) as the message printed on console.

## Miscellaneous ##

`RuntimeUtil.getPID()` returns ProcessID of current JVM Process as String.

**Force GC**

```
java.lang.System.gc()
```

This method suggests JVM to make best effort to invoke garbage collection;<br>
But it doesn't gaurantee that garbage collection is done;<br>
<br>
<pre><code>import jlibs.core.lang.RuntimeUtil;<br>
<br>
RuntimeUtil.gc();<br>
</code></pre>

This method gaurantees that garbage collection is done (unline <code>System.gc()</code>)<br>
<br>
<pre><code>RuntimeUtil.gc(10);<br>
</code></pre>

This method gaurantees that garbage collection is done atleast 10 times;<br>
<br>
JVM doesn't guarantee that GC is called on JVM exit; To gaurantee use:<br>
<br>
<pre><code>RuntimeUtil.gcOnExit();<br>
</code></pre>


<b>Finalization</b>

JVM doesn't guarantee that <code>Object.finalize()</code> is always called when an object is claimed for GC;<br>
for more details see <a href='http://java.sun.com/developer/technicalArticles/javase/finalization/'>this</a>

Moreover <code>finalize()</code> is somewhat messy. In finalize what if we pass self reference to some other<br>
object. Then there is a chance that, after <code>finalize()</code> is called, the object might be reachable<br>
and thus cannot be GCed. In future when GC finds that the same object is no longer reachable,<br>
it will call <code>finalize()</code> method again. Thus there is a chance that <code>finalize()</code> could be called<br>
more than once for a given object.<br>
<br>
We have <code>jlibs.core.lang.ref.Finalizer</code> to perform better finalization.<br>
<br>
for example see following method:<br>
<pre><code>public &lt;T&gt; WeakReference&lt;T&gt; track(T obj, Runnable runnable)<br>
</code></pre>
this method requests to execute given <code>runnable</code> after <code>obj</code> has been garbage collected;<br>
<br>
See following example usecase:<br>
<br>
<pre><code>public class MyImage{<br>
    private int nativeImgHandle;<br>
    private Point pos;<br>
    private Dimension dim;<br>
<br>
    public MyImage(String path){<br>
        nativeImgHandle = createNativeImageHandle(path);<br>
        Finalizer.INSTANCE.track(this, new MyImageDisposer(nativeImgHandle));<br>
    }<br>
<br>
    private int createNativeImageHandle(String path){<br>
        return 10;<br>
    }<br>
}<br>
<br>
class MyImageDisposer implements Runnable{<br>
    private int nativeImgHandle;<br>
<br>
    public MyImageDisposer(int nativeImgHandle){<br>
        this.nativeImgHandle = nativeImgHandle;<br>
    }<br>
<br>
    @Override<br>
    public void run(){<br>
        System.out.println("releasing native image handle");<br>
        //OS.releaseHandle(nativeImgHandle);<br>
    }<br>
}<br>
</code></pre>

Here <code>MyImage</code> creates a <code>nativeImageHandle</code> which needs to be released when<br>
it is garbage collected;<br>
<br>
To test that Finalizer is really doing its work:<br>
<br>
<pre><code>public static void main(String[] args){<br>
    RuntimeUtil.gcOnExit();<br>
    MyImage img = new MyImage("test.gif");<br>
    img = null;<br>
    System.out.println("exiting jvm");<br>
}<br>
</code></pre>
running this will print:<br>
<pre><code>exiting jvm<br>
releasing native image handle<br>
</code></pre>

If you comment the line <code>RuntimeUtil.gcOnExit();</code> in main method,<br>
then you will notice that <code>releasing native image handle</code> is not printed.<br>
This shows that JVM might not do GC on JVM exit.<br>
<br>
There is also another variation of track method, for example:<br>
<pre><code>Finalizer.INSTANCE.track(this, SoftReference.class, new MyImageDisposer(nativeImgHandle));<br>
</code></pre>

The second argument tells what type of reference to be created. The earlier <code>track(...)</code> method<br>
creates WeakReference.<br>
<br>
These <code>match(...)</code> methods return the <code>java.lang.ref.Reference</code> object created.<br>
<br>
<b>Simple Profiling</b>

When you are working on a big project, at some point of time you will face memory issues.<br>
Then normally people start using profilers to detect memory leak. The problem with this, by<br>
that time the amount of code in project might be quite big, to analyze when, where and why memory leaked.<br>
<br>
Rather than profiling at later time, I suggest to profile your app frequently, when you think<br>
that the code written today might have possible memory leak.<br>
<br>
Instead of using full fledged profiler, I normally prefer the following way:<br>
<br>
Let us think of scenario. When user open a file in your application, you parse the file into<br>
some sort of java objects and show it to user using some UI. when user close that UI, it is<br>
quite obvious that the java objects which are created to hold the data of file should be eligible<br>
for garbage collection. To check whether your code has memory leak, you can do:<br>
<br>
<pre><code>public MyCompany parse(File file){<br>
    MyCompany company = new MyCompany();<br>
    // parse file and fill data into company;<br>
    Finalizer.INSTANCE.trackGC(company, "MyCompany["+file+"]");<br>
}<br>
<br>
<br>
public void onEditorClose(){<br>
    // close the editor<br>
    RuntimeUtil.gc(10);<br>
}<br>
</code></pre>

now you launch your application, open a file, do some ui actions and then close the ui.<br>
if you see the following line in your console:<br>
<pre><code>JLibsFinalizer: 'MyCompany[C:\sample\test.xml]' got garbage collected.<br>
</code></pre>

you know that your code has no memory leak. If it doesn't print this line, then try again<br>
after changing <code>RuntimeUtil.gc(10);</code> to <code>RuntimeUtil.gc(20);</code>.<br>
<br>
If still you don't get the line on console, pick some full-fledged profiler and fix memory<br>
leak.(don't post pone this)<br>
<br>
There is also another variation of <code>trackGC(....)</code>:<br>
<br>
<pre><code>public void trackGC(Object obj)<br>
</code></pre>

The second argument in earlier version of <code>track(...)</code> is a readable message that need to be printed when given object is GCed;<br>
<br>
In <code>trackGC(obj)</code> the message defaults to <code>classname@identityhascode</code> (for example <code>MyCompany@13243</code>)<br>
<br>
<b>Miscellaneous</b>

<code>RuntimeUtil.getPID()</code> returns ProcessID of current JVM Process as String;
Let us have a look at following code;
```

// should be called only in EDT
public Result method1(){
    ...
}

public void method2(Result result){
    ...    
}

public void perform(){
    Result result = method1();
    method2(result);
}
```

Assume that, `perform()` method can be called by any thread (gui or non-gui thread);<br>
and <code>method2()</code> requires the <code>result</code> returned by <code>method1()</code>;<br>
<br>
then we must change <code>perform()</code> method as below:<br>
<pre><code>public void perform(){<br>
    final Result result[] = { null };<br>
    Runnable runnable = new Runnable(){<br>
        public void run(){<br>
            result[0] = method1();<br>
        }<br>
    };<br>
    if(SwingUtilities.isEventDispatchThread())<br>
        runnable.run();<br>
    else{<br>
        try{<br>
            SwingUtilities.invokeAndWait(runnable);<br>
        }catch(Exception ex){<br>
            throw new RuntimeException(ex);<br>
        }<br>
    }<br>
<br>
    method2(result[0]);<br>
}<br>
</code></pre>

with jlibs, you can do:<br>
<br>
<pre><code>import jlibs.swing.EDT;<br>
import jlibs.core.lang.Task;<br>
<br>
public void perform(){<br>
    Result result = EDT.INSTANCE.execute(new Task&lt;Result&gt;(){<br>
        public Result run(){<br>
            return method1();<br>
        }<br>
    });<br>
    method2(result);<br>
}<br>
</code></pre>

This avoids code clutter, and makes more readable than earlier one;<br>
<br>
Let us say <code>method1()</code> can throw checked exception; Then <code>perform()</code> implementation would look as below:<br>
<br>
<pre><code>// should be called only in EDT<br>
public Result method1() throws SomeException{<br>
    ....<br>
}<br>
<br>
public void perform() throws SomeException{<br>
    final SomeException ex[] = { null };<br>
    final Result result[] = { null };<br>
    Runnable runnable = new Runnable(){<br>
        public void run(){<br>
            try{<br>
                result[0] = method1();<br>
            }catch(SomeException e){<br>
                ex[0] = e;<br>
            }<br>
        }<br>
    };<br>
    if(SwingUtilities.isEventDispatchThread())<br>
        runnable.run();<br>
    else{<br>
        try{<br>
            SwingUtilities.invokeAndWait(runnable);<br>
        }catch(Exception e){<br>
            throw new RuntimeException(e);<br>
        }<br>
    }<br>
    if(ex[0]!=null)<br>
        throw ex[0];<br>
<br>
    method2(result[0]);<br>
}<br>
</code></pre>

with JLibs, you can do:<br>
<br>
<pre><code>import jlibs.swing.EDT;<br>
import jlibs.core.lang.ThrowableTask;<br>
<br>
public void perform() throws SomeException{<br>
    Result result = EDT.INSTANCE.execute(new ThrowableTask&lt;Result, SomeException&gt;(SomeException.class){<br>
        public Result run() throws SomeException{<br>
            return method1();<br>
        }<br>
    });<br>
    method2(result);<br>
}<br>
</code></pre>

<hr />

<code>ThrowableTask&lt;R, E&gt;</code>:<br>
<ul><li>R - return type<br>
</li><li>E - exception that can be thrown</li></ul>

the constructor of <code>ThrowableTask</code> requires the <code>Exception</code> class as argument, so that it can use <code>Class.isInstance(ex)</code>;<br>
<br>
The abstract method to be overridden is:<br>
<pre><code>public R run() throws E<br>
</code></pre>

<code>ThrowableTask</code> can be converted to <code>Runnable</code> as below:<br>
<pre><code>ThrowableTask task = ....<br>
Runnable runnable = task.asRunnable();<br>
</code></pre>

<code>Task&lt;R&gt;</code> is just a subclass of <code>ThrowableTask&lt;R, E&gt;</code> where E is <code>RuntimeException</code>;<br>
<br>
<hr />

<code>EDT</code> extends <code>jlibs.core.lang.ThreadTasker</code>

like <code>EDT</code>, you can create a subclass of <code>ThreadTasker</code> for <code>SWT</code> and use it;<br>
<br>
<b>Miscellaneous</b>

<pre><code>EDT.INSTANCE.isValid() // to check whether calling thread is event dispatch thread or not<br>
EDT.INSTANCE.executeLater(Runnable) // equivalent to SwingUtilities.invokeLater(Runnable)<br>
</code></pre>
Let us say you have Employee class defined as below:

```
public class Employee{
    private String name;
    private String email;
    private int age;
}
```

We can implement `hasCode()` as follows:

```
@Override
public int hashCode(){
    int hashCode = age;
    if(name!=null)
        hashCode += name.hashCode();
    if(email!=null)
        hashCode += email.hashCode();
    return hashCode;
}
```

i.e, we simply add hashCode of each member and return it;<br>
while doing this, we should be careful to check whether a member is null;<br>
<br>
Using <a href='http://code.google.com/p/jlibs/source/browse/trunk/core/src/jlibs/core/lang/Util.java#85'>Util.hashCode(Object...)</a> you can simplify this as below:<br>
<br>
<pre><code>import jlibs.core.lang.Util;<br>
<br>
@Override<br>
public int hashCode(){<br>
    return age + Util.hashCode(name, email);<br>
}<br>
</code></pre>

Here JVM implitly creates an array containing name and email. If you want to avoid extra array creation, you could use <a href='http://code.google.com/p/jlibs/source/browse/trunk/core/src/jlibs/core/lang/Util.java#53'>Util.hashCode(Object)</a> as below:<br>
<br>
<pre><code>import jlibs.core.lang.Util;<br>
<br>
@Override<br>
public int hashCode(){<br>
    return age + Util.hashCode(name) + Util.hashCode(email);<br>
}<br>
</code></pre>

<b>NOTE:</b> <code>Util.hashCode(...)</code> uses <code>java.util.Arrays</code> to compute hashCode for arrays.
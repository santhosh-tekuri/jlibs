First see the example:

```
import jlibs.core.lang.Ansi;

Ansi ansi = new Ansi(Ansi.Attribute.BRIGHT, Ansi.Color.BLUE, Ansi.Color.WHITE);
ansi.out("hello ansi world");
```

will produce following output:

![http://jlibs.googlecode.com/svn/trunk/wiki/Ansi1.png](http://jlibs.googlecode.com/svn/trunk/wiki/Ansi1.png)

**Ansi:**

`jlibs.core.lang.Ansi` constains two enums one for attributes and another for colors;

`Ansi.Attribute` enum contains various attributes like BRIGHT, DIM, UNDERLINE etc,.<br>
<code>Ansi.Color</code> enum contains various colors like BLACK, RED, GREEN etc,.<br>
<br>
The constructor of <code>Ansi</code> is of the following form:<br>
<pre><code>public Ansi(Attribute attr, Color foreground, Color background)<br>
</code></pre>

all arguments can take null. for example if forground is null, it won't change foreground color.<br>
<br>
Wrapping a string with special ansi control sequences and printing onto ansi supported console will result the ansi colors shown.<br>
<br>
<pre><code>String msg = ansi.colorize("hello ansi world"); // msg is original string wrapped with ansi control sequences<br>
System.out.println(msg);<br>
</code></pre>

The above code will print <code>hello ansi world</code> in specified ansi format.<br>
<br>
the above two lines can be replaced with <code>Ansi.out(...)</code> as follows:<br>
<pre><code>ansi.out("hello ansi world");<br>
</code></pre>

similarly there are various handy methods provided in <code>Ansi</code>:<br>
<pre><code>Ansi.outln(String msg); // print given msg to console in ansi format followed by new line<br>
Ansi.outFormat(String format, Object... args)<br>
<br>
// to print to System.err<br>
Ansi.err(String msg);<br>
Ansi.errln(String msg);<br>
Ansi.errFormat(String format, Object... args);<br>
</code></pre>

<b>Ansi Support</b>:<br>
<br>
Ansi might not be supported on all systems. Ansi is mostly supported by all unix operating systems.<br>
<br>
<code>Ansi.SUPPORTED</code> is a final boolean, that can be used to check whether your console supports Ansi format;<br>
<br>
<code>Ansi</code> class uses simple checks to decide whether ansi is supported or not. Sometimes it may do wrong guess. In such cases you can override its decision using following system property:<br>
<pre><code>-DAnsi=true<br>
or<br>
-DAnsi=false<br>
</code></pre>

if <code>Ansi.SUPPORTED</code> is false, any ansi method will not produce ansi control sequences. so you can safely use:<br>
<pre><code>ansi.out("hello ansi world");<br>
</code></pre>
irrespective of ansi is supported or not. if ansi is not supported, this will simply do <code>System.out.print("hello ansi world")</code>

<b>Ansi Formatter:</b>

JLibs provides an implementation of <code>java.util.logging.Formatter</code>, to use ansi in logging. This class is:<br>
<pre><code>jlibs.core.util.logging.AnsiFormatter;<br>
</code></pre>

Let us see usage of <code>AnsiFormatter</code>:<br>
<pre><code>Logger logger = LogManager.getLogManager().getLogger("");<br>
logger.setLevel(Level.FINEST);<br>
<br>
Handler handler = logger.getHandlers()[0];<br>
handler.setLevel(Level.FINEST);<br>
handler.setFormatter(new AnsiFormatter());<br>
<br>
for(Level level: map.keySet())<br>
    logger.log(level, "this is "+level+" message");<br>
</code></pre>

will produce following output:<br>
<br>
<img src='http://jlibs.googlecode.com/svn/trunk/wiki/Ansi2.png' />

<code>AnsiFormatter</code> has public constants to access <code>Ansi</code> instance used for each level:<br>
<pre><code>public class AnsiFormatter extends Formatter{<br>
    public static final Ansi SEVERE;<br>
    public static final Ansi WARNING;<br>
    public static final Ansi INFO;<br>
    public static final Ansi CONFIG;<br>
    public static final Ansi FINE;<br>
    public static final Ansi FINER;<br>
    public static final Ansi FINEST;<br>
<br>
    ...<br>
}<br>
</code></pre>

These constants are made public, so that you can use them any where. for example you can do:<br>
<br>
<pre><code>import static jlibs.core.util.logging.AnsiFormatter.*;<br>
<br>
SEVERE.out("User authentication failed");<br>
</code></pre>

The colors used by <code>AnsiFormatter</code> for any level can be changed to match you taste. To do this you need to create a properties file. for example:<br>
<pre><code># myansi.properties<br>
<br>
SEVERE=DIM;RED;GREEN<br>
WARNING=BRIGHT;RED;YELLOW<br>
</code></pre>

Now use following system property:<br>
<pre><code>-Dansiformatter.default=/path/to/myansi.properties<br>
</code></pre>

Each entry in this property file is to be given as below:<br>
<br>
key will be the level name;<br>
value is semicolon(<code>;</code>) separated values, where each argument is considered as argument to <code>Ansi</code> class constructor.<br>

if any agument is null, you still need to specify empty argument. for example:<br>
<pre><code>SEVERE=DIM;;GREEN # foreground is not specified<br>
</code></pre>

In your properties file, you don't need to specify entries for each level. you can specify entries only for those levels that you want to change;<br>
<br>
Your comments are welcomed.
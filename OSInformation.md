`jlibs.core.lang.OS`([source](http://code.google.com/p/jlibs/source/browse/trunk/core/src/jlibs/core/lang/OS.java)) is an enum from `JLibs`;<br>

This enum contains values for each type of OS. for example: WINDOWS_NT, WINDOWS_98, LINUX, SOLARIS etc...<br>
<br>
<b>Usage:</b>

<pre><code>import jlibs.core.lang.OS;<br>
<br>
OS myos = OS.get();<br>
System.out.println(myos);<br>
</code></pre>

On my laptop, it prints <code>MAC</code>

you can also check whether your os is windows or unix;<br>
<br>
<pre><code>OS myos = OS.get();<br>
System.out.println("isWindows: "+myos.isWindows());<br>
System.out.println("isUnix: "+myos.isUnix());<br>
</code></pre>

On my laptop, it prints:<br>
<pre><code>isWindows: false<br>
isUnix: true<br>
</code></pre>

When your OS is not recognized, <code>OS.get()</code> returns <code>OS.OTHER</code>;<br>
<br>
There is another usefult method which might be required rarely;<br>
<br>
<pre><code>String osName = System.getProperty("os.name");<br>
OS os = OS.get(osName);<br>
</code></pre>

This might be handy, if your app is distributed and and want to find out the os of other JVM process<br>
<br>
your comments are welcomed;
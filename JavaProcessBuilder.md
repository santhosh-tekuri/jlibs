Java comes with [java.lang.ProcessBuilder](http://java.sun.com/j2se/1.5.0/docs/api/java/lang/ProcessBuilder.html) to make process creation easier;<br>
Similarly JLibs comes with <a href='http://code.google.com/p/jlibs/source/browse/trunk/core/src/main/java/jlibs/core/lang/JavaProcessBuilder.java'>jlibs.core.lang.JavaProcessBuilder</a> to make java process creation easier;<br>
<br>
<code>JavaProcessBuilder</code> simply manages collection of java process attributes and help your to create command out of it.<br>
<pre><code>JavaProcessBuilder jvm = new JavaProcessBuilder();<br>
</code></pre>

<code>JavaProcessBuilder</code> is preconfigured with current java home and current working directory initialy.<br>
you can change them as below:<br>
<pre><code>jvm.javaHome(new File("c:/jdk5")); // to configure java home<br>
jvm.workingDir(new File("c:/myProject")); // to configure working directory<br>
</code></pre>

to configure various attributes:<br>
<pre><code>// to configure classpath<br>
jvm.classpath("lib/jlibs-core.jar") // relative path from configured working dir<br>
   .classpath(new File("c:/myproject/lib/jlibs-xml.jar");<br>
<br>
// to get configured classpath<br>
List&lt;File&gt; classpath = jvm.classpath();<br>
<br>
// to configure additional classpath<br>
jvm.endorsedDir("lib/endorsed")<br>
   .extDir("lib/ext")<br>
   .libraryPath("lib/native")<br>
   .bootClasspath("lib/boot/xerces.jar")<br>
   .appendBootClasspath("lib/boot/xalan.jar")<br>
   .prependBootClasspath("lib/boot/dom.jar");<br>
<br>
// to configure System Properties<br>
jvm.systemProperty("myprop", "myvalue")<br>
   .systemProperty("myflag");<br>
<br>
// to configure heap and vmtype<br>
jvm.initialHeap(512); // or jvm.initialHeap("512m");<br>
jvm.maxHeap(1024); // or jvm.maxHeap("1024m");<br>
jvm.client(); // to use -client<br>
jvm.server(); // to use -server<br>
<br>
// to configure remote debugging<br>
jvm.debugPort(7000)<br>
   .debugSuspend(true);<br>
<br>
// to configure any additional jvm args<br>
jvm.jvmArg("-Xgc:somealgo");<br>
<br>
// to configure mainclass and its arguments<br>
jvm.mainClass("example.MyTest")<br>
   .arg("-xvf")<br>
   .arg("testDir");<br>
<br>
// to get the created command:<br>
String command[] = jvm.command();<br>
<br>
// to launch it<br>
Process p = jvm.launch(system.out, System.err);<br>
</code></pre>
the two arguments to launch specify to which process output and error streams to be redirected.<br>
These arguments can be null.<br>
<br>
Your comments are appreciated;
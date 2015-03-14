It is common approach to provide os specific scripts(bat/sh files) to launch java application.<br>
We populate classpath, system properties etc, in bat/sh file and launch java process.<br>
Problem with this approach is, classpath etc are inlined in both bat and sh files.<br>
You need to ensure that classpath etc in both bat/sh files are always in sync. This is a tedious task;<br>
<br>
There any many products which help you to create native launchers for java application. These products<br>
take classpath etc as input and generate os specific launchers. Problem with this approach is,<br>
If the end user wants to tweak some of those parameters like heap-size add database specific jars to classpath.<br>
Because classpath etc informations is embedded inside generated native launcher, it is not possible to change them;<br>
<br>
<code>JLibs</code> comes with <code>JLaunch</code> scripts which help java developers to overcome these problems.<br>
<br>
you will find two script files:<br>
<ul><li><a href='http://code.google.com/p/jlibs/source/browse/trunk/core/bin/launcher/jlaunch.bat'>jlaunch.bat</a> - for windows<br>
</li><li><a href='http://code.google.com/p/jlibs/source/browse/trunk/core/bin/launcher/jlaunch.sh'>jlaunch.sh</a>  - for <code>*</code>nix and mac</li></ul>

these script files take a conf file as argument. This conf file contains all the information like classpath, system properties etc.<br>
<br>
to launch your java app on windows:<br>
<pre><code>path/to/jlauncher.bat path/to/myapp.conf<br>
</code></pre>

to launch the same app on <b>nix:<br>
<pre><code>path/to/jlauncher.sh path/to/myapp.conf<br>
</code></pre></b>

the working directory of java process launched will the directory in which conf file is present.<br>
<br>
any additional arguments after conf file are passed as main class arguments<br>
<pre><code>path/to/jlauncher.sh path/to/myapp.conf arg1 arg2 arg3<br>
</code></pre>

let us see a sample conf file:<br>
<pre><code>&lt;java.classpath&gt;<br>
engine.jar<br>
ui.jar<br>
jdom.jar<br>
<br>
&lt;java.endorsed.dirs&gt;<br>
lib/endorsed<br>
<br>
&lt;java.ext.dirs&gt;<br>
lib/ext<br>
<br>
&lt;java.library.path&gt;<br>
lib<br>
<br>
&lt;java.system.props&gt;<br>
# use mx4j mbean server<br>
javax.management.builder.initial=mx4j.server.MX4JMBeanServerBuilder<br>
<br>
# logging properties<br>
java.util.logging=mylog.properties<br>
<br>
&lt;java.bootclasspath.prepend&gt;<br>
#lib/mxj4.jar<br>
<br>
&lt;jvm.args&gt;<br>
-showversion<br>
com.foo.MyApplication<br>
-open<br>
some/file<br>
</code></pre>

The conf file is composed of following sections:<br>
<pre><code>&lt;java.classpath&gt;<br>
&lt;java.endorsed.dirs&gt;<br>
&lt;java.ext.dirs&gt;<br>
&lt;java.library.path&gt;<br>
&lt;java.system.props&gt;<br>
&lt;java.bootclasspath&gt;<br>
&lt;java.bootclasspath.prepend&gt;<br>
&lt;java.bootclasspath.append&gt;<br>
&lt;jvm.args&gt;<br>
</code></pre>

<ul><li>Each section is followed by its configuration. You can omit the sections you don't need.<br>
</li><li>the order of sections in conf file is not significant.<br>
</li><li>A line starting with <code>#</code> is treated as comment.<br>
</li><li>Any empty lines in conf file are ignored.<br>
</li><li>Any line which is not in section is ignored (i.e lines before first section are ignored)<br>
</li><li>wildcards are not supported. i,e. you shouldn't write <code>lib/*.jar</code>
</li><li>Environment variables can be used in conf file. But it makes your conf file no more platform independent.</li></ul>

<hr />

Rather than asking end user to type "jlaunch.sh myapp.conf" to launch your java app, you can create simple wrappers as explained below:<br>
<br>
<a href='http://code.google.com/p/jlibs/source/browse/trunk/utils/xsd-viewer.sh'>xsd-viewer.sh</a> and<br>
<a href='http://code.google.com/p/jlibs/source/browse/trunk/utils/xsd-viewer.bat'>xsd-viewer.bat</a>
from jlibs which launch xsd-viewer swing application.<br>
<br>
<a href='http://code.google.com/p/jlibs/source/browse/trunk/utils/xsd-viewer.sh'>xsd-viewer.sh</a> is a shell script cum conf file.<br>
<a href='http://code.google.com/p/jlibs/source/browse/trunk/utils/xsd-viewer.bat'>xsd-viewer.bat</a> passes xsd-viewer.sh as conf file to jlaunch.bat; i.e you should ship xsd-viewer.sh along with xsd-viewer.bat for windows;<br>
<br>
Your comments are appreciated;
`greplog` is a command line tool providing grep functionality to log files.

```
java -jar jlibs-greplog.jar <header-file> <filter-file> [-printIndex] <log-file> ...
```

Each logrecord starts with header and message.
```
05:05:39.101 main INFO  BOOTSTRAP Initializing server...
```

in above logrecord `05:05:39.101 main INFO  BOOTSTRAP` is header and "Initializing server..." is message.

First you need to create `header.xml` which defines the format of logrecord header.

header.xml
```
<header>
    <pattern>(\d\d:\d\d:\d\d.\d\d\d) ([^\s]+)\s+([^\s]+)\s+([^\s]+)</pattern>
    <field group="1" name="timestamp"/>
    <field group="2" name="thread"/>
    <field group="3" name="level"/>
    <field group="4" name="module"/>
</header>
```

the `<pattern>` element defines java regex to mach the logrecord header.<br>
here the regex is using groups to identify the fields in logrecord header.<br>
the first group <code>(\d\d:\d\d:\d\d.\d\d\d)</code> matches the timestamp. i.e, <code>05:05:39.101</code><br>
the second group <code>([^\s]+)</code> matches the thread. i.e, <code>main</code><br>
the third group <code>([^\s]+)</code> matches the level. i.e, <code>INFO</code><br>
the fourth group <code>([^\s]+)</code> matches the module. i.e, <code>BOOTSTRAP</code><br>

each field is named using <code>&lt;field&gt;</code> element by specifying the group index.<br>
<br>
Now you have create <code>filter.xml</code> which tells what to extract from log file.<br>
<br>
filter.xml<br>
<pre><code>&lt;or&gt;<br>
    &lt;field name="level"&gt;WARNING&lt;/field&gt;<br>
    &lt;field name="level"&gt;ERROR&lt;/field&gt;<br>
&lt;/or&gt;<br>
</code></pre>
now run greplog command as follows:<br>
<pre><code>java -jar jlibs-greplog.jar header.xml filter.xml server.log<br>
</code></pre>

<hr />

to grep all logrecords from iothreads assumming you have named io threads as iothread1, iothread2, iothread3 etc<br>
the filter.xml will be:<br>
<pre><code>&lt;field name="thread"&gt;iothread\d+&lt;/field&gt;<br>
</code></pre>

i.e we used java regex <code>iothread\d+</code> to match iothreads.<br>
note that the regex should completely match the field value in logrecord.<br>
<br>
<hr />

to grep all logrecords which contain exception stacktraces:<br>
<br>
<pre><code>&lt;message&gt;(?m)(?s)^.*Exception: .*$&lt;/message&gt;<br>
</code></pre>

this greps all logrecords whose message contains <code>Exception: </code><br>
Note: flags <code>(?m)</code> and <code>(?s)</code> are required because message can be multi-line<br>
<br>
<hr />

to grep all logrecords from non-iothreads:<br>
<pre><code>&lt;not&gt;<br>
    &lt;field name="thread"&gt;iothread\d+&lt;/field&gt;<br>
&lt;/not&gt;<br>
</code></pre>

<hr />
to grep all logrecords following the first logrecord whose message contains<br>
<code>NullPointerException: </code>

<pre><code>&lt;following includeSelf="true"&gt;<br>
    &lt;message&gt;(?m)(?s)^.*NullPointerException: .*$&lt;/message&gt;<br>
&lt;/following&gt;<br>
</code></pre>

similarly to grep all logrecords preceding the first logrecord whose message contains<br>
<code>NullPointerException: </code>

<pre><code>&lt;preceding includeSelf="true"&gt;<br>
    &lt;message&gt;(?m)(?s)^.*NullPointerException: .*$&lt;/message&gt;<br>
&lt;/preceding&gt;<br>
</code></pre>

<hr />
Let us say your application appends log to existing server.log when restarted.<br>
and you want to grep only logrecords from last jvm session.<br>
<br>
Assuming that your application logs a specific message while booting up.<br>
for example:<br>
<pre><code>05:05:39.101 main INFO  BOOTSTRAP Initializing server...<br>
.....<br>
.....<br>
.....<br>
06:05:39.101 main INFO  BOOTSTRAP Initializing server...<br>
.....<br>
.....<br>
.....<br>
07:05:39.101 main INFO  BOOTSTRAP Initializing server...<br>
.....<br>
.....<br>
.....<br>
08:05:39.101 main INFO  BOOTSTRAP Initializing server...<br>
.....<br>
.....<br>
.....<br>
</code></pre>

first grep these specifc log record using following filter.xml<br>
<pre><code>&lt;and&gt;<br>
    &lt;field name="thread"&gt;BOOTSTRAP&lt;/field&gt;<br>
    &lt;message&gt;(?m)(?s)^Initializing server\.\.\.$&lt;/message&gt;    <br>
&lt;/and&gt;<br>
</code></pre>

use <code>printIndex</code> argument:<br>
<pre><code>java -jar jlibs-greplog.jar header.xml filter.xml -printIndex server.log<br>
</code></pre>

this argument prints index of logrecord before each logrecord as follows:<br>
<pre><code>1 05:05:39.101 main INFO  BOOTSTRAP Initializing server...<br>
1934 06:05:39.101 main INFO  BOOTSTRAP Initializing server...<br>
2749 07:05:39.101 main INFO  BOOTSTRAP Initializing server...<br>
3678 08:05:39.101 main INFO  BOOTSTRAP Initializing server...<br>
</code></pre>

from the above output, we know that the 3678th logrecord is the first<br>
logrecord in last jvm session.<br>
<br>
now to grep only logrecords from last jvm session:<br>
<pre><code>&lt;following includeSelf="true"&gt;<br>
    &lt;index&gt;3678&lt;/index&gt;<br>
&lt;/following&gt;<br>
</code></pre>
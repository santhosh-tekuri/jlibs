Pumping means reading from given `input` and writing to specified `output`;

**IOUtil:**

`jlibs.core.io.IOUtil` provides various utility method for pumping streams;

let us say you want to read some File as `String`. You can do:

```
import jlibs.core.io.IOUtil;

StringWriter writer = new StringWriter();
IOUtil.pump(new FileReader(file), writer, true, true);
String content = writer.toString();
```

let us see arguments for `pump(...)` method:

argument 1 = input <br>
argument 2 = output<br>
argument 3 = boolean that specifies whether input should be closed<br>
argument 4 = boolean that specifies whether output should be closed<br>
i.e,<br>
<br>
<code>pump(input, output, closeIn, closeOut)</code>

To simplify code, <code>pump(...)</code> method returns output; So the above code could be written in single line as follows:<br>
<pre><code>String content = IOUtil.pump(new FileReader(file), writer, true, true).toString();<br>
</code></pre>

if output is not specified, it defaults to <code>StringWriter</code>. so the same code can be written as:<br>
<pre><code>String content = IOUtil.pump(new FileReader(file), true).toString(); // second arg is closeIn<br>
</code></pre>

similar versions of pump(...) methods are available for byte-streams also;<br>
<br>
Let us see how these methods simplify some code;<br>
<br>
to copy file:<br>
<pre><code>IOUtil.pump(new FileInputStream(fromFile), new FileOutputStream(toFile), true, true);<br>
</code></pre>

to create zip file:<br>
<pre><code>ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));<br>
for(File file: files){<br>
    zipOut.putNextEntry(new ZipEntry(file.getName());<br>
    IOUtil.pump(new FileInputStream(file), zipOut, true, false); // note that last arg is false<br>
    zipOut.closeEntry();<br>
}<br>
zipOut.close();<br>
</code></pre>

to create file with given string:<br>
<pre><code>String content = ...<br>
IOUtil.pump(new StringReader(content), new FileWriter(file), true, true);<br>
</code></pre>

to read a file content into byte array:<br>
<pre><code>byte bytes[] = IOUtil.pump(new FileInputStream(file), true).toByteArray(); // output defaults to ByteArrayOutputStream<br>
</code></pre>

<a href='http://code.google.com/p/jlibs/source/browse/trunk/core/src/jlibs/core/io/IOUtil.java'>source</a>

<hr />

<b>Bytes:</b>

Let us say we have an object of type <code>Company</code>:<br>
<br>
<pre><code>Company company = ...;<br>
</code></pre>

Now you would like to clone <code>company</code> object. but assume that <code>Company</code> doesn't implement <code>Cloneable</code> interface but implements <code>Serializable</code>.<br>
<br>
Now you can do cloning using serialization as follows:<br>
<pre><code>ByteArrayOutputStream bout = new ByteArrayOutputStream();<br>
ObjectOutputStream objOut = new ObjectOutputStream(bout);<br>
objOut.writeObject(company);<br>
objOut.close();<br>
<br>
ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray()); // bottle-neck<br>
ObjectInputStream objIn = new ObjectInputStream(bin);<br>
Company copyOfCompany = (Company)objIn.readObject();<br>
objIn.close();<br>
</code></pre>

In above code see the line commented <code>bottle-neck</code>:<br>
<pre><code>ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());<br>
</code></pre>

let us say bout contains 1024 bytes i.e, one MB. the statement <code>bout.toByteArray()</code> returns a cloned array,<br>
rather than returning the orignal array; So it needs one more MB of free memory in heap;<br>
<br>
To avoid this you can use <code>jlibs.core.io.Bytes</code>:<br>
<br>
<pre><code>import jlibs.core.io.Bytes;<br>
<br>
Bytes bytes = new Bytes();<br>
<br>
ObjectOutputStream objOut = new ObjectOutputStream(bytes.out());<br>
objOut.writeObject(obj);<br>
objOut.close();<br>
<br>
ObjectInputStream objIn = new ObjectInputStream(bytes.in());<br>
Company copyOfCompany = (Company)objIn.readObject();<br>
objIn.close();<br>
</code></pre>

<code>Bytes</code> can be treated as a buffer of bytes, which can be written/read using <code>Bytes.out()/Bytes.in()</code>.<br>
It reuses the same byte array for both output and input;<br>
<br>
<a href='http://code.google.com/p/jlibs/source/browse/trunk/core/src/jlibs/core/io/Bytes.java'>source</a>

<hr />

<b>PumpedInputStream and PumpedReader:</b>

You can use <code>PipedInputStream</code> and <code>PipedOutputStream</code> in above example, so that you can write and read concurrently using fixed byte buffer.<br>
<br>
<pre><code>final Company company = new Company();<br>
PipedInputStream pipedIn = new PipedInputStream();<br>
final PipedOutputStream pipedOut = new PipedOutputStream(pipedIn);<br>
final IOException ioEx[] = { null };<br>
new Thread(){<br>
    @Override<br>
    public void run(){<br>
        try{<br>
            ObjectOutputStream objOut = new ObjectOutputStream(pipedOut);<br>
            objOut.writeObject(company);<br>
            objOut.close();<br>
        }catch(IOException ex){<br>
            ioEx[0] = ex;<br>
        }<br>
    }<br>
}.start();<br>
ObjectInputStream objIn = new ObjectInputStream(pipedIn);<br>
Company copyOfCompany = (Company)objIn.readObject();<br>
objIn.close();<br>
if(ioEx[0]!=null)<br>
    throw ioEx[0];<br>
</code></pre>

In <code>JLibs</code>, you can use <code>PumpedInputStream</code>:<br>
<br>
<pre><code>import jlibs.core.io.PumpedInputStream;<br>
<br>
final Company company = new Company();<br>
PumpedInputStream in = new PumpedInputStream(){<br>
    @Override<br>
    protected void pump(PipedOutputStream out) throws Exception{<br>
        ObjectOutputStream objOut = new ObjectOutputStream(out);<br>
        objOut.writeObject(company);<br>
        objOut.close();<br>
    }<br>
}.start(); // start() will spawn new thread<br>
<br>
ObjectInputStream objIn = new ObjectInputStream(in);<br>
Company copyOfCompany = (Company)objIn.readObject();<br>
objIn.close(); // any exceptions occurred in pump(...) are thrown by close()<br>
</code></pre>

<code>PumpedInputStream</code> is an abstract class with following abstract method:<br>
<pre><code>protected abstract void pump(PipedOutputStream out) throws Exception;<br>
</code></pre>

This method implementation should write data into <code>out</code> which is passed as argument and close it;<br>
<br>
Any exceptions thrown by pump(...) are wrapped in <code>IOException</code> and rethrown by <code>PumpedInputStream.close()</code>;<br>
<br>
<code>PumpedInputStream</code> implements <code>Runnable</code> which is supposed to be run in thread. You can use <code>PumpedInputStream.start()</code> method to start thread or spawn thread implicitly.<br>
<code>start()</code> method returns self reference;<br>
<pre><code>    public PumpedInputStream start();<br>
</code></pre>

I like to used <code>PumpedInputStream</code> rather than <code>PipedInputStream/PipedOutputStream/Thread</code> because:<br>
<ul><li>it doesn't clutter the exising flow of code<br>
</li><li>exception handling is better;</li></ul>

<a href='http://code.google.com/p/jlibs/source/browse/trunk/core/src/jlibs/core/io/PumpedInputStream.java'>source</a><br><br>

There is also <code>jlibs.core.io.PumpedReader</code> for charater-streams.<br>
<br>
your comments are welcomed;
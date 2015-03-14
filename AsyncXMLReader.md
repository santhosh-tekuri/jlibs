[AsyncXMLReader](http://code.google.com/p/jlibs/source/browse/trunk/xml/src/main/java/jlibs/xml/sax/async/AsyncXMLReader.java) implements `org.xml.sax.XMLReader` interface.<br>
So it can be used just like any standard SAX Parser implementation.<br>
<br>
But It also supports non-blocking parsing, So that it can be used with NIO.<br>
<br>
Let us see how to use it in non-blocking mode.<br>
<br>
Rather than using <code>org.xml.sax.InputSource</code>, we use <a href='http://code.google.com/p/jlibs/source/browse/trunk/xml/src/main/java/jlibs/xml/sax/async/ChannelInputSource.java'>ChannelInputSource</a>:<br>
<pre><code>InputSource source = new ChannelInputSource(socketChannel);<br>
</code></pre>

Note that <code>ChannelInputSource</code> is used for non-blocking input.<br>
<br>
Now instead of doing <code>reader.parse(source)</code> we will do the following:<br>
<pre><code>Feeder feeder = reader.createFeeder(source);<br>
feeder = feeder.feed();<br>
</code></pre>

We are using non-blocking input, thus we are not feeding entire xml document to the reader in one shot.<br>
we should keep feeding the reader, as an when data is ready to read from socketChannel. This concept is<br>
abstracted into <code>Feeder</code> class. When data is ready to be read from socketChannel, we call <code>feeder.feed()</code>.<br>
When feeding is complete, <code>feeder.feed()</code> returns null;<br>
<br>
But one interesing thing to be noted is, <code>feeder.feed()</code> returns <code>Feeder</code>. The returned <code>Feeder</code> could be<br>
the same <code>feeder</code> object or different one. For example here the feeder returned might be for external dtd, <br>
in case you have resolved external dtd with another <code>ChannelInputSource</code>.<br>
<br>
<pre><code>SocketChannel channel = (SocketChannel)feeder.byteChannel();<br>
// enable read interest on channel<br>
</code></pre>
<code>feeder.byteChannel()</code> returns the actual <code>ReadableByteChannel</code> on which more input required. Now you can add read<br>
interest on this channel, and when data is ready to read, you can resume feeding by calling <code>feeder.feed()</code>.<br>
<br>
You can use both blocking and non-blocking inputs intermixed. for example xml document might be feeded from<br>
non-blocking input where as the dtd it is referring may be from a blocking input (local file)
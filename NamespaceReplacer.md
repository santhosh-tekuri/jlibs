[NamespaceReplacer](http://code.google.com/p/jlibs/source/browse/trunk/xml/src/main/java/jlibs/xml/sax/NamespaceReplacer.java) extends
[SAXDelegate](http://code.google.com/p/jlibs/source/browse/trunk/xml/src/main/java/jlibs/xml/sax/SAXDelegate.java).

`SAXDelegate` is used to chain sax handlers. `SAXDelegate` implements all sax handlers `ContentHandler`, `LexicalHandler` etc.
It provides `setXXXHandler(…)` to support chaining.

First create a map specifying the namespaces to be replaced.
```
Map<String, String> namespaces = new HashMap<String, String>();
namespaces.put("http://schemas.xmlsoap.org/soap/envelope/", "http://www.w3.org/2003/05/soap-envelope");
namespaces.put("http://jlibs.org", "");
```
here we are trying to replace `http://schemas.xmlsoap.org/soap/envelope/` with `http://www.w3.org/2003/05/soap-envelope`<br>
and <code>http://jlibs.org</code> with empty namespace.<br>
<br>
Now create <code>NamespaceReplacer</code>:<br>
<pre><code>import jlibs.xml.sax.NamespaceReplacer;<br>
<br>
NamespaceReplacer nsReplacer = new NamespaceReplacer(namespaces);<br>
</code></pre>

let us say you have <code>MyDefaultHandler</code> which is supposed to recieve sax events.<br>
Rather than registering <code>MyDefaultHandler</code> with <code>SAXParser</code>, you register <code>nsReplacer</code><br>
with <code>SAXParser</code>, and <code>MyDefaultHandler</code> is registered with <code>nsReplacer</code>.<br>
<br>
<pre><code>import javax.xml.parsers.SAXParser;<br>
<br>
MyDefaultHandler myHandler = new MyDefaultHandler();<br>
nsReplacer.setContentHandler(myHandler)<br>
SAXParser parser = createNamespaceAwareSAXParser();<br>
parser.parse(xmlFile, nsReplacer);<br>
</code></pre>

Now sax events recieved by <code>myHandler</code> will have namespaces replaced.
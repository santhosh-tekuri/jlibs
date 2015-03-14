**Standard Namespaces**

[Namespaces](http://code.google.com/p/jlibs/source/browse/trunk/xml/src/jlibs/xml/Namespaces.java) class contains constants to many standard namespaces which we use in our daily projects.
```
import static jlibs.xml.Namespaces.*;

System.out.println(URI_SOAP);
System.out.println(URI_WSDL);
```

It can also suggest standard prefixes used for those namespaces.
```
String prefix = Namespaces.suggestPrefix(URI_WSDL);
System.out.println(prefix); // prints "wsdl"
```

`Namespaces.getSuggested()` returns `java.util.Properties` object where key is URI and value is suggested prefix;


---


**MyNamespaceSupport**

[MyNamespaceSupport](http://code.google.com/p/jlibs/source/browse/trunk/xml/src/jlibs/xml/sax/helpers/MyNamespaceSupport.java) extends `org.xml.sax.helpers.NamespaceSupport` to provide some handy features.

It is tedious to find prefix from `org.xml.sax.helpers.NamespaceSupport`. for example:

```
import org.xml.sax.helpers.NamespaceSupport;

NamespaceSupport nsSupport = new NamespaceSupport();
nsSupport.declarePrefix("ns1", "http://mynamespace1");
nsSupport.declarePrefix("", "http://mynamespace2");

// to find prefix of "http://mynamespace1"
System.out.println(nsSupport.getPrefix("http://mynamespace1")); // prints "ns1"

// let us try to find prefix of "http://mynamespace2"
System.out.println(nsSupport.getPrefix("http://mynamespace2")); // prints null
```

As per javadoc, `NamespaceSupport.getPrefix(...)` returns prefixes only for those namespaces which are bound to non-empty prefix;<br>
So in order to find prefix properly, we should do something like this:<br>
<pre><code>String prefix = nsSupport.getPrefix(uri);<br>
if(prefix==null){<br>
    if(nsSupport.getURI("").equals(uri))<br>
        prefix = "";<br>
}<br>
System.out.println("prefix: "+prefix);<br>
</code></pre>

<code>MyNamespaceSupport</code> provides handy method <code>findPrefix(uri)</code> for this:<br>
<pre><code>import jlibs.xml.sax.helper.MyNamespaceSupport;<br>
<br>
MyNamespaceSupport nsSupport = new MyNamespaceSupport();<br>
nsSupport.declarePrefix("", "http://mynamespace2");<br>
<br>
// let us try to find prefix of "http://mynamespace2"<br>
System.out.println(nsSupport.findPrefix("http://mynamespace2")); // prints ""<br>
</code></pre>

It also helps you in suggesting prefixes;<br>
<pre><code>String prefix = nsSupport.declarePrefix("http://www.google.com"); // we are asking to generate prefix<br>
System.out.println(prefix); // prints "google"    <br>
</code></pre>

You can also override the suggested prefix for any uri, as below:<br>
<pre><code>nsSupport.suggestPrefix("tns", "http://java.sun.com"); // we are just suggesting (not declaring)<br>
....<br>
....<br>
String prefix = nsSupport.declare("http://java.sun.com");<br>
System.out.println(prefix); // prints "tns"    <br>
</code></pre>

<hr />

<b>DefaultNamespaceContext</b>

JAXP provides interface <code>org.xml.sax.helpers.NamespaceContext</code> interface, but doesn't provide any concrete implementation for this.<br>
This interface is used to pass namespace and prefix binding for xpath evaluation (see <code>javax.xml.xpath.XPath</code>)<br>
<br>
<a href='http://code.google.com/p/jlibs/source/browse/trunk/xml/src/jlibs/xml/DefaultNamespaceContext.java'>DefaultNamespaceContext</a> implements this interface.<br>

<pre><code>DefaultNamespaceContext nsContext = new DefaultNamespaceContext();<br>
nsContext.declarePrefix("ns1", "http://namespace1");<br>
String prefix = nsContext.declarePrefix("http://www.google.com"); // prefix will be suggested to "google"<br>
<br>
// now we can use nsContext for xpath evaluation<br>
</code></pre>

You can also create DefaultNamespaceContext from MyNamespaceSupport;<br>
<pre><code>MyNamespaceSupport nsSupport = .....<br>
......<br>
DefaultNamespaceContext nsContext = new DefaultNamespaceContext(nsSupport);<br>
</code></pre>

<hr />

<b>ClarkName</b>

<a href='http://code.google.com/p/jlibs/source/browse/trunk/xml/src/jlibs/xml/ClarkName.java'>ClarkName</a> is String notation for <code>javax.xml.namespaces.QName</code>;<br>
<br>
<pre><code>import jlibs.xml.ClarkName;<br>
<br>
String str1 = ClarkName.valueOf("htpp://namespace1", "elem1"); // str1 will be "{http://namespace1}elem1"<br>
String str2 = ClarkName.valueOf("", "elem2"); // str2 will be "elem1"<br>
</code></pre>

This is compatible with <code>QName.toString()</code>;<br>
you can the namespace and local-name back:<br>
<pre><code>String clarkName = ClarkName.valueOf("htpp://namespace1", "elem1");<br>
String str[] = ClarkName.split(clarkName);<br>
System.out.println(str[0]); // prints "http://namespace1"<br>
System.out.println(str[2]); // prints "elem1"<br>
</code></pre>

support you want to save location of particular element in xpath form: for example,<br>
<pre><code>ns1:elem1/ns2:elem2/ns2:elem3<br>
where<br>
ns1="http://namespace1"<br>
ns2="http://namespace2"<br>
ns3="http://namespace3"<br>
</code></pre>

you can convert into raw xpath by inlining namespaces into xpath:<br>
<pre><code>{http://namespace1}elem1/{http://namespace2}elem2/{http://namespace1}elem3<br>
</code></pre>
this form will be easier to save to some file and restore it back; ClarkName provides handy method to split such paths.<br>
<pre><code>String clarkPath = "{http://namespace1}elem1/{http://namespace2}elem2/{http://namespace1}elem3";<br>
String clarkNames[] = ClarkName.splitPath(clarkPath);<br>
</code></pre>

Your comments are welcomed;
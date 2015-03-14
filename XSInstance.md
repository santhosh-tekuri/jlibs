You can find command line utility `xsd-instance.sh` in `jlibs/bin` directory.

```
xsd-instance.sh <xsd-file> [root-element]
```

for example:
```
xsd-instance.sh purchase-order.xsd {http://jlibs.org}PurchaseOrder
```

here `root-element` is optional. If not specified, It will guess root-element<br>
from the given schema. It it finds multiple root elements in schema, then it <br>
prompts to chose one among them.<br>
<br>
you can configure various options in <code>jlibs/bin/xsd-instance.properties</code>
<pre><code>minimumElementsGenerated=2<br>
maximumElementsGenerated=4<br>
minimumListItemsGenerated=2<br>
maximumListItemsGenerated=4<br>
<br>
# for following properties value can be always/never/random<br>
generateOptionalElements=always<br>
generateOptionalAttributes=always<br>
generateFixedAttributes=always<br>
generateDefaultAttributes=always<br>
</code></pre>
<hr />
<h3>Programmatic Usage</h3>

First you parse the schema file as follows:<br>
<pre><code>import jlibs.xml.xsd.XSParser;<br>
import org.apache.xerces.xs.*;<br>
<br>
XSModel xsModel = new XSParser().parse("purchageOrder.xsd");<br>
</code></pre>

Create an instanceof <code>XSInstance</code> and configure various options<br>
<pre><code>import jlibs.xml.xsd.XSInstance;<br>
<br>
XSInstance xsInstance = new XSInstance();<br>
xsInstance.minimumElementsGenerated = 2;<br>
xsInstance.maximumElementsGenerated = 4;<br>
xsInstance.generateOptionalElements = Boolean.TRUE; // null means random<br>
</code></pre>

now genreate the sample xml as follows:<br>
<pre><code>import jlibs.xml.sax.XMLDocument;<br>
<br>
QName rootElement = new QName("http://jlibs.org", "PurchaseOrder");<br>
XMLDocument sampleXml = new XMLDocument(new StreamResult(System.out), true, 4, null);<br>
xsInstance.generate(xsModel, rootElement, sampleXml);<br>
</code></pre>

Your comments are appreciated;
[XMLDog](http://code.google.com/p/jlibs/source/browse/trunk/xml/src/jlibs/xmldog/sax/dog/XMLDog.java) is a dog that is trained to sniff xml documents.

We give set of xpaths to `XMLDog` and ask to sniff some xml document. It uses SAX and with one pass over the document it evaluates all the given xpaths.

Whether it is Xalan/XMLDog, first we need to define `javax.xml.namespace.NamespaceContext`. This interface defines the binding for prefix to uri.
```
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;

DefaultNamespaceContext nsContext = new DefaultNamespaceContext(); // an implementation of javax.xml.namespace.NamespaceContext
nsContext.declarePrefix("xsd", Namespaces.URI_XSD);
```

Now create an instance of `XMLDOG`, and add the xpaths that need to be evaluated. Note that `XMLDog` can evaluate multiple xpaths in single SAX parse of given xml document.
```
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.expr.Expression;

XMLDog dog = new XMLDog(nsContext);

Expression xpath1 = dog.addXPath("/xs:schema/@targetNamespace");
Expression xpath2 = dog.addXPath("/xs:schema/xs:complexType/@name");
Expression xpath3 = dog.addXPath("/xs:schema/xs:*/@name");
```
When you add xpath to `XMLDog`, it returns `Expression` object. This object is the compiled xpath.

you can get the original xpath from `XPath` using `getXPath()`:
```
System.out.println(xpath1.getXPath()); // prints "/xs:schema/@targetNamespace"
```

you can ask `Expression` about its result type;
```
import javax.xml.namespace.QName;

QName resultType = xpath1.resultType.qname;
System.out.println(resultType); // prints "{http://www.w3.org/1999/XSL/Transform}NODESET"
```
The `QName` returned will be one of constants in `javax.xml.xpath.XPathConstants`.

To evaluate given xpaths on some xml document:
```
import jlibs.xml.sax.dog.XPathResults;

XPathResults results = dog.sniff(new InputSource("note.xml"));
```

`XPathResults` object will contain the results of all xpath evaluations.

to get result of particular xpath:
```
object result = results.getResult(xpath1);
```
The return type of `getResult(XPath)` will be `java.lang.Object`;

Depending on the `XPath.resultType()`, this result can be safely cased to a particular type.

Below is the actual result Type for each resultType returned by `XPath`:

| `XPath.resultType()` | result can be cast to |
|:---------------------|:----------------------|
| `XPathConstants.STRING` | `java.lang.String` |
| `XPathConstants.BOOLEAN` | `java.lang.Boolean` |
| `XPathConstants.NUMBER` | `java.lang.Double` |
| `XPathConstants.NODESET` | `java.util.Collection<NodeItem>` |

`NodeItem` represens an xml node in xml document; `NodeItem` has following properties.

`NodeItem.type`:
> returns type of xml node. will be one of following constants in `NodeType`:
> > COMMENT, PI, DOCUMENT, ELEMENT, ATTRIBUTE, NAMESPACE, TEXT;
`NodeItem.location`:

> returns unique xpath to the xml node. ex: `/xs:schema[1]/xs:complexType[1]/@name`<br>
<blockquote>the prefixes in this xpath can be resolved using <code>results.getNamespaceContext()</code><br>
this can be used to create DOM, in case you need it.<br>
<code>NodeItem.value</code>, <code>NodeItem.localName</code>, <code>NodeItem.namespaceURI</code>, <code>NodeItem.qualifiedName</code>:<br>
return value/localName/namespaceURI/qualifiedName of the xml node it represens.</blockquote>

<code>NodeItem.toString()</code> simply returns its <code>location</code>.<br>
<br>
<code>XPathResults</code> has handy print method to print results to given <code>java.io.PrintStream</code>:<br>
<pre><code>results.print(dog.getExpressions(), System.out);<br>
</code></pre>
will print:<br>
<pre><code>XPath: /xs:schema/@targetNamespace<br>
      1: /xs:schema[1]/@targetNamespace<br>
<br>
XPath: /xs:schema/xs:complexType/@name<br>
      1: /xs:schema[1]/xs:complexType[1]/@name<br>
<br>
XPath: /xs:schema/xs:*/@name<br>
      1: /xs:schema[1]/xs:element[1]/@name<br>
      2: /xs:schema[1]/xs:element[2]/@name<br>
      3: /xs:schema[1]/xs:element[3]/@name<br>
      4: /xs:schema[1]/xs:element[4]/@name<br>
      5: /xs:schema[1]/xs:complexType[1]/@name<br>
</code></pre>
<hr />
<b>Multi Threading:</b>

<code>XMLDog</code> supports multi-hreading. You can add multiple xpaths once, <br>
and sniff multiple documents with same <code>XMLDog</code> instance parallely;<br>
<hr />
<b>XPath Support:</b>

<code>XMLDog</code> supports subset of XPath 1.0;<br>
<br>
Axises supported are:<br>
<ul><li>self<br>
</li><li>child<br>
</li><li>descendant<br>
</li><li>descendant-or-self<br>
</li><li>following<br>
</li><li>following-sibling<br>
</li><li>attribute<br>
</li><li>namespace</li></ul>

Except <code>id()</code>, rest of the functions are supported.<br>
it supports predicates and all operators.<br>
<br>
<code>XMLDog</code> will tell you clearly, if given xpath is not supprted; for example:<br>
<pre><code>XPath xpath = dog.add("/xs:schema/../@targetNamespace", 1);<br>
</code></pre>
throws following exception:<br>
<pre><code>java.lang.UnsupportedOperationException: unsupported axis: parent<br>
</code></pre>

This will be very useful. for example you can first try using <code>XMLDog</code> and if it throws <code>UnsupportedOperationException</code>,<br>
then you can fallback to use <code>DOM</code>
<hr />
<b>DOM Results</b>

By default <code>XMLDog</code> does not construct dom nodes for results.<br>
You can configure for DOM results as follows:<br>
<pre><code>import package jlibs.xml.sax.dog.sniff.Event;<br>
<br>
Event event = dog.createEvent();<br>
results = new XPathResults(event);<br>
event.setListener(results);<br>
event.setXMLBuilder(new DOMBuilder());<br>
dog.sniff(event, new InputSource("note.xml"));<br>
<br>
List&lt;NodeItem&gt; items = (List&lt;NodeItem&gt;)results.getResult(xpath1)<br>
</code></pre>

you can get the dom node for a given <code>NodeItem</code> as follows:<br>
<pre><code>NodeItem item = ...<br>
org.w3c.dom.Node domNode = (org.w3c.dom.Node)item.xml;<br>
</code></pre>

Note that, dom nodes are created only for portions of xml which are hit by xpaths.<br>
<br>
<code>Event.setXMLBuilder(...)</code> takes an argument of type <code>jlibs.xml.sax.dog.sniff.XMLBuilder</code>.<br>
So if you want <code>JDom</code> to be construction instead of <code>DOM</code>, write an implementation of <code>XMLBuilder</code><br>
which constructs <code>JDom</code> and use it.<br>
<hr />
<b>Instant Results</b>

<code>XPathResults</code> object holds results of all xpaths in memory. This might not be feasible always.<br>

Let us say, you are searching employees.xml for employees with more that 5 years of experience.<br>
if employees.xml has 10000 employees and there are more than 5000 employees who match this criteria.<br>
Holding 5000 employees in memory may cause <code>OutOfMemoryError</code>.<br>
<br>
To solve this problem, you register your own <code>InstantEvaluationListener</code> with <code>Event</code>. Then your listener<br>
will be notified as soon as an employee with specified criteria is found. Thus you can process that employee<br>
and discard it.<br>
<br>
<pre><code>import jlibs.xml.sax.dog.expr.InstantEvaluationListener;<br>
<br>
Event event = dog.createEvent();<br>
event.setXMLBuilder(new DOMBuilder());<br>
event.setListener(new InstantEvaluationListener(){<br>
    @Override<br>
    public void onNodeHit(Expression expression, NodeItem nodeItem){<br>
        org.w3c.dom.Node node = (org.w3c.dom.Node)nodeItem.xml;<br>
        System.out.println("XPath: "+expression.getXPath()+" has hit: "+node);<br>
    }<br>
<br>
    @Override<br>
    public void finishedNodeSet(Expression expression){<br>
        System.out.println("Finished Nodeset: "+expression.getXPath());<br>
    }<br>
<br>
    @Override<br>
    public void onResult(Expression expression, Object result){<br>
        // this method is called only for xpaths which returns primitive result<br>
        // i.e result will be one of String, Boolean, Double<br>
        System.out.println("XPath: "+expression.getXPath()+" result: "+result);<br>
    }<br>
});<br>
dog.sniff(event, new InputSource("note.xml"), false/*useSTAX*/); // this version sniff method returns void<br>
</code></pre>
<hr />
You can use variables and custom functions in xpath.<br>
For this you have to use following constructor:<br>
<pre><code>import javax.xml.namespace.NamespaceContext;<br>
import javax.xml.xpath.XPathVariableResolver;<br>
import javax.xml.xpath.XPathFunctionResolver;<br>
<br>
NamespaceContext nsContext = ...;<br>
XPathVariableResolver variableResolver = ...;<br>
XPathFunctionResolver functionResolver = ...;<br>
<br>
XMLDog dog = new XMLDogContext(nsContext, variableResolver, functionResolver);<br>
</code></pre>

Note that functions are not supposed to expect arguments of type <code>NodeSet</code>
<hr />
<b>Command Line Utility</b>

You can find <code>xmldog.sh/xmldog.bat</code> in $JLIBS_HOME/bin directory<br>
<br>
This will be usefull to play with <code>XMLDog</code> with various xml documents/xpaths<br>
<hr />
<b>Conformance</b>

The XMLDog results conforms to the XPath-Spec. It is coverted by <code>jlibs.xml.sax.dog.tests.XPathConformanceTest</code>

You can look <a href='http://code.google.com/p/jlibs/source/browse/trunk/xmldog/resources/xpaths.xml'>here</a>, to see the type of xpaths it has been tested.<br>
<br>
You can find <code>xmldog-conformance.sh/xmldog-conformance.bat</code> in jlibs installation,<br>
<hr />
<b>Performance:</b>

You can find <code>xmldog-performance.sh/xmldog-performance.bat</code> in jlibs installation,<br>
which can be used to test <code>XMLDog</code> perfomace against <code>Xalan</code> that is shipped with JDK.<br>
This test reads config from <a href='http://code.google.com/p/jlibs/source/browse/trunk/examples/resources/xpaths.xml'>xpaths.xml</a> from jlibs installation.<br>
<br>
Here is sample output of this performance test;<br>
<pre><code>Average Execution Time over 20 runs:<br>
--------------------------------------------------------------------------------<br>
                                   File | XPaths XMLDog  SAXON   Diff Percentage<br>
--------------------------------------------------------------------------------<br>
            resources/xmlFiles/note.xml |    290     35     84    -49   -2.42<br>
          resources/xmlFiles/simple.xml |     29      4     13     -8   -3.04<br>
       resources/xmlFiles/positions.xml |    110     16     27    -10   -1.64<br>
          resources/xmlFiles/sample.xml |   2197    195    176     18   +1.11<br>
         resources/xmlFiles/sample1.xml |   2197     25     76    -51   -3.04<br>
         resources/xmlFiles/sample2.xml |   2197     29     77    -47   -2.59<br>
         resources/xmlFiles/sample3.xml |   2197     28     72    -44   -2.53<br>
         resources/xmlFiles/numbers.xml |     83      1      3     -2   -2.22<br>
      resources/xmlFiles/underscore.xml |     80      2      3     -1   -1.41<br>
        resources/xmlFiles/contents.xml |    160      4      9     -4   -1.99<br>
              resources/xmlFiles/pi.xml |     31      0      1     -1   -2.63<br>
        resources/xmlFiles/evaluate.xml |     40      1      2      0   -1.32<br>
             resources/xmlFiles/web.xml |    431      7     23    -16   -3.30<br>
            resources/xmlFiles/fibo.xml |     94      4     12     -7   -2.58<br>
resources/xmlFiles/defaultNamespace.xml |     80      0      2     -1   -3.28<br>
      resources/xmlFiles/namespaces.xml |    150      3      6     -3   -1.78<br>
            resources/xmlFiles/text.xml |     35      0      1      0   -2.16<br>
    resources/xmlFiles/organization.xml |    110      4      6     -2   -1.53<br>
        resources/xmlFiles/moreover.xml |    130     10     16     -6   -1.61<br>
              resources/xmlFiles/id.xml |     40      0      2     -1   -3.45<br>
        resources/xmlFiles/much_ado.xml |     78     14     17     -3   -1.24<br>
             resources/xmlFiles/sum.xml |     17      0      1     -1   -5.00<br>
  resources/xmlFiles/purchase_order.xml |    510      7     18    -11   -2.61<br>
            resources/xmlFiles/roof.xml |     20      0      1     -1   -2.64<br>
            resources/xmlFiles/nitf.xml |     60      1      3     -1   -2.28<br>
         resources/xmlFiles/message.xml |     10      0      1     -1   -3.42<br>
            resources/xmlFiles/lang.xml |     80      1      3     -2   -2.96<br>
  resources/xmlFiles/testNamespaces.xml |     22      0      1     -1   -3.65<br>
            resources/xmlFiles/test.xml |     20      0      1     -1   -3.06<br>
          resources/xmlFiles/jaxen3.xml |     10      0      1      0   -2.73<br>
         resources/xmlFiles/jaxen24.xml |     30      0      1      0   -3.01<br>
             resources/xmlFiles/pi2.xml |     10      0      1     -1   -5.00<br>
         resources/xmlFiles/library.xml |     20      1      1      0   -1.17<br>
            resources/xmlFiles/axis.xml |     32      0      1      0   -2.30<br>
               resources/xmlFiles/t.xml |     10      0      0      0   -2.58<br>
--------------------------------------------------------------------------------<br>
                                  Total |  11610    410    683   -273   -1.67<br>
</code></pre>

It shows that XMLDog is faster than Saxon9(1.67 times).<br>
<br>
The source code of testcase is <a href='http://code.google.com/p/jlibs/source/browse/trunk/xmldog/test/jlibs/xml/sax/dog/tests/XPathPerformanceTest.java'>here</a>
<hr />

<b>Future:</b>
<ul><li>ability to specify min number of items required in NODESET</li></ul>

I am looking forward to know, who are interested in <code>XMLDog</code>, and why/where you are using. This will give me some boost-up to add more features. Because it takes most of my free time.<br>
<br>
Your comments are welcomed;
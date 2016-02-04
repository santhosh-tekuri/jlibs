---
title: XMLDog
layout: default
---

# XMLDog: SAX based XPath 1.0 Engine #

`XMLDog` is a dog that is trained to sniff xml documents.

We give set of xpaths to `XMLDog` and ask to sniff some xml document. It uses SAX and with one pass over the document it evaluates all the given xpaths.

Whether it is Xalan/XMLDog, first we need to define `javax.xml.namespace.NamespaceContext`. This interface defines the binding for prefix to uri.

~~~java
import jlibs.xml.DefaultNamespaceContext;
import jlibs.xml.Namespaces;

DefaultNamespaceContext nsContext = new DefaultNamespaceContext(); // an implementation of javax.xml.namespace.NamespaceContext
nsContext.declarePrefix("xsd", Namespaces.URI_XSD);
~~~

Now create an instance of `XMLDOG`, and add the xpaths that need to be evaluated. Note that `XMLDog` can evaluate multiple xpaths in single SAX parse of given xml document.

~~~java
import jlibs.xml.sax.dog.XMLDog;
import jlibs.xml.sax.dog.expr.Expression;

XMLDog dog = new XMLDog(nsContext);

Expression xpath1 = dog.addXPath("/xs:schema/@targetNamespace");
Expression xpath2 = dog.addXPath("/xs:schema/xs:complexType/@name");
Expression xpath3 = dog.addXPath("/xs:schema/xs:*/@name");
~~~

When you add xpath to `XMLDog`, it returns `Expression` object. This object is the compiled xpath.

you can get the original xpath using `Expression#getXPath()`:

~~~java
System.out.println(xpath1.getXPath()); // prints "/xs:schema/@targetNamespace"
~~~

you can ask `Expression` about its result type;

~~~java
import javax.xml.namespace.QName;

QName resultType = xpath1.resultType.qname;
System.out.println(resultType); // prints "{http://www.w3.org/1999/XSL/Transform}NODESET"
~~~

The `QName` returned will be one of constants in `javax.xml.xpath.XPathConstants`.

To evaluate given xpaths on some xml document:

~~~java
import jlibs.xml.sax.dog.XPathResults;

XPathResults results = dog.sniff(new InputSource("note.xml"));
~~~

`XPathResults` object will contain the results of all xpath evaluations.

to get result of particular xpath:

~~~java
object result = results.getResult(xpath1);
~~~

The return type of `getResult(XPath)` will be `java.lang.Object`.

Depending on the `XPath.resultType()`, this result can be safely cased to a particular type.

Below is the actual result Type for each resultType returned by `XPath`:

~~~
| XPath.resultType()     | result can be cast to          |
|------------------------|--------------------------------|
| XPathConstants.STRING  | java.lang.String               |
| XPathConstants.BOOLEAN | java.lang.Boolean              |
| XPathConstants.NUMBER  | java.lang.Double               |
| XPathConstants.NODESET | java.util.Collection<NodeItem> |
~~~

`NodeItem` represens an xml node in xml document; `NodeItem` has following properties.

`NodeItem.type`:

returns type of xml node. will be one of following constants in `NodeType`:
`COMMENT`, `PI`, `DOCUMENT`, `ELEMENT`, `ATTRIBUTE`, `NAMESPACE`, `TEXT`;

`NodeItem.location`:

returns unique xpath to the xml node. ex: `/xs:schema[1]/xs:complexType[1]/@name`  
the prefixes in this xpath can be resolved using `results.getNamespaceContext()`  
this can be used to create DOM, in case you need it.

`NodeItem.value`, `NodeItem.localName`, `NodeItem.namespaceURI`, `NodeItem.qualifiedName`:

return value/localName/namespaceURI/qualifiedName of the xml node it represens

`NodeItem.toString()` simply returns its `location`.

`XPathResults` has handy print method to print results to given `java.io.PrintStream`:

~~~java
results.print(dog.getExpressions(), System.out);
~~~

will print:

~~~java
XPath: /xs:schema/@targetNamespace
      1: /xs:schema[1]/@targetNamespace

XPath: /xs:schema/xs:complexType/@name
      1: /xs:schema[1]/xs:complexType[1]/@name

XPath: /xs:schema/xs:*/@name
      1: /xs:schema[1]/xs:element[1]/@name
      2: /xs:schema[1]/xs:element[2]/@name
      3: /xs:schema[1]/xs:element[3]/@name
      4: /xs:schema[1]/xs:element[4]/@name
      5: /xs:schema[1]/xs:complexType[1]/@name
~~~

## Multi Threading ##

`XMLDog` supports multi-hreading. You can add multiple xpaths once, 
and sniff multiple documents with same `XMLDog` instance parallely;

## XPath Support ##

<code>XMLDog</code> supports subset of XPath 1.0

Axises supported are:

- self
- child
- descendant
- descendant-or-self
- following
- following-sibling
- attribute
- namespace

Except `id()`, rest of the xpath functions are supported.

it supports predicates and all operators.

`XMLDog` will tell you clearly, if given xpath is not supprted; for example:

~~~java
XPath xpath = dog.add("/xs:schema/../@targetNamespace", 1);
~~~

throws following exception:

~~~
java.lang.UnsupportedOperationException: unsupported axis: parent
~~~

This will be very useful. for example you can first try using `XMLDog` and if it throws `UnsupportedOperationException`,
then you can fallback to use traditional xpath engine.

## DOM Results ##

By default `XMLDog` does not construct dom nodes for results.  
You can configure for DOM results as follows:

~~~java
import package jlibs.xml.sax.dog.sniff.Event;

Event event = dog.createEvent();
results = new XPathResults(event);
event.setListener(results);
event.setXMLBuilder(new DOMBuilder());
dog.sniff(event, new InputSource("note.xml"));

List<NodeItem> items = (List<NodeItem>)results.getResult(xpath1)
for(NodeItem item: items){
    org.w3c.dom.Node domNode = (org.w3c.dom.Node)item.xml;
    // do something with domNode
}
~~~

Note that, dom nodes are created only for portions of xml which are hit by xpaths. Thus you can run xpaths on large documents.

`Event.setXMLBuilder(...)` takes an argument of type `jlibs.xml.sax.dog.sniff.XMLBuilder`.  
So if you want `JDom` to be constructed instead of DOM, write an implementation of `XMLBuilder`  
which constructs JDom and use it.

## Instant Results ##

`XPathResults` object holds results of all xpaths in memory. This might not be feasible always.

Let us say, you are searching employees.xml for employees with more that 5 years of experience.  
if employees.xml has 10000 employees and there are more than 5000 employees who match this criteria.  
Holding 5000 employees in memory may cause `OutOfMemoryError`.

To solve this problem, you register your own `InstantEvaluationListener` with `Event`. This listener
will be notified as soon as an employee with specified criteria is found. Thus you can process that employee
and discard it.

~~~java
import jlibs.xml.sax.dog.expr.InstantEvaluationListener;

Event event = dog.createEvent();
event.setXMLBuilder(new DOMBuilder());
event.setListener(new InstantEvaluationListener(){
    @Override
    public void onNodeHit(Expression expression, NodeItem nodeItem){
        org.w3c.dom.Node node = (org.w3c.dom.Node)nodeItem.xml;
        System.out.println("XPath: "+expression.getXPath()+" has hit: "+node);
    }

    @Override<br>
    public void finishedNodeSet(Expression expression){
        System.out.println("Finished Nodeset: "+expression.getXPath());
    }

    @Override
    public void onResult(Expression expression, Object result){
        // this method is called only for xpaths which returns primitive result
        // i.e result will be one of String, Boolean, Double
        System.out.println("XPath: "+expression.getXPath()+" result: "+result);
    }
});

dog.sniff(event, new InputSource("note.xml"), false/*useSTAX*/); // this version sniff method returns void
~~~

## Variables and Custom Functions ##

~~~java
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathVariableResolver;
import javax.xml.xpath.XPathFunctionResolver;

NamespaceContext nsContext = ...;
XPathVariableResolver variableResolver = ...;
XPathFunctionResolver functionResolver = ...;

XMLDog dog = new XMLDogContext(nsContext, variableResolver, functionResolver);
~~~

Note that functions are not supposed to expect arguments of type `NodeSet`

## Command Line Utility ##

You can find `xmldog.sh/xmldog.bat` in jlibs distribution

This will be usefull to play with `XMLDog` with various xml documents/xpaths

## Conformance ##

The XMLDog results conforms to the XPath-Spec. It is coverted by `jlibs.xml.sax.dog.tests.XPathConformanceTest`

You can look [xpaths.xml](https://github.com/santhosh-tekuri/jlibs/blob/master/examples/resources/xpaths.xml), to see the xpaths that have been tested.

You can find <code>xmldog-conformance.sh/xmldog-conformance.bat</code> in jlibs distribution

## Performance ##

You can find <code>xmldog-performance.sh/xmldog-performance.bat</code> in jlibs distribution,
which can be used to test <code>XMLDog</code> perfomace against <code>Xalan</code> that is shipped with JDK.
This test reads configuration from [xpaths.xml](https://github.com/santhosh-tekuri/jlibs/blob/master/examples/resources/xpaths.xml) from jlibs distribution.

Here is sample output of this performance test;

~~~
Average Execution Time over 20 runs:
--------------------------------------------------------------------------------
                                   File | XPaths XMLDog  SAXON   Diff Percentage
--------------------------------------------------------------------------------
            resources/xmlFiles/note.xml |    290     35     84    -49   -2.42
          resources/xmlFiles/simple.xml |     29      4     13     -8   -3.04
       resources/xmlFiles/positions.xml |    110     16     27    -10   -1.64
          resources/xmlFiles/sample.xml |   2197    195    176     18   +1.11
         resources/xmlFiles/sample1.xml |   2197     25     76    -51   -3.04
         resources/xmlFiles/sample2.xml |   2197     29     77    -47   -2.59
         resources/xmlFiles/sample3.xml |   2197     28     72    -44   -2.53
         resources/xmlFiles/numbers.xml |     83      1      3     -2   -2.22
      resources/xmlFiles/underscore.xml |     80      2      3     -1   -1.41
        resources/xmlFiles/contents.xml |    160      4      9     -4   -1.99
              resources/xmlFiles/pi.xml |     31      0      1     -1   -2.63
        resources/xmlFiles/evaluate.xml |     40      1      2      0   -1.32
             resources/xmlFiles/web.xml |    431      7     23    -16   -3.30
            resources/xmlFiles/fibo.xml |     94      4     12     -7   -2.58
resources/xmlFiles/defaultNamespace.xml |     80      0      2     -1   -3.28
      resources/xmlFiles/namespaces.xml |    150      3      6     -3   -1.78
            resources/xmlFiles/text.xml |     35      0      1      0   -2.16
    resources/xmlFiles/organization.xml |    110      4      6     -2   -1.53
        resources/xmlFiles/moreover.xml |    130     10     16     -6   -1.61
              resources/xmlFiles/id.xml |     40      0      2     -1   -3.45
        resources/xmlFiles/much_ado.xml |     78     14     17     -3   -1.24
             resources/xmlFiles/sum.xml |     17      0      1     -1   -5.00
  resources/xmlFiles/purchase_order.xml |    510      7     18    -11   -2.61
            resources/xmlFiles/roof.xml |     20      0      1     -1   -2.64
            resources/xmlFiles/nitf.xml |     60      1      3     -1   -2.28
         resources/xmlFiles/message.xml |     10      0      1     -1   -3.42
            resources/xmlFiles/lang.xml |     80      1      3     -2   -2.96
  resources/xmlFiles/testNamespaces.xml |     22      0      1     -1   -3.65
            resources/xmlFiles/test.xml |     20      0      1     -1   -3.06
          resources/xmlFiles/jaxen3.xml |     10      0      1      0   -2.73
         resources/xmlFiles/jaxen24.xml |     30      0      1      0   -3.01
             resources/xmlFiles/pi2.xml |     10      0      1     -1   -5.00
         resources/xmlFiles/library.xml |     20      1      1      0   -1.17
            resources/xmlFiles/axis.xml |     32      0      1      0   -2.30
               resources/xmlFiles/t.xml |     10      0      0      0   -2.58
--------------------------------------------------------------------------------
                                  Total |  11610    410    683   -273   -1.67
~~~

It shows that XMLDog is faster than Saxon9(1.67 times).

The source code of testcase is <a href='http://code.google.com/p/jlibs/source/browse/trunk/xmldog/test/jlibs/xml/sax/dog/tests/XPathPerformanceTest.java'>here</a>

## TODO ##

- ability to specify min number of items required in NODESET

---
title: Namespaces
layout: default
---

# Working with Namespaces #

## Standard Namespaces ##

[Namespaces](https://github.com/santhosh-tekuri/jlibs/blob/master/xml/src/main/java/jlibs/xml/Namespaces.java) class contains constants to many standard namespaces which we use in our daily projects.

~~~java
import static jlibs.xml.Namespaces.*;

System.out.println(URI_SOAP);
System.out.println(URI_WSDL);
~~~

It can also suggest standard prefixes used for those namespaces.

~~~java
String prefix = Namespaces.suggestPrefix(URI_WSDL);
System.out.println(prefix); // prints "wsdl"
~~~

`Namespaces.getSuggested()` returns `java.util.Properties` object where key is URI and value is suggested prefix;

## MyNamespaceSupport ##

[MyNamespaceSupport](https://github.com/santhosh-tekuri/jlibs/blob/master/xml/src/main/java/jlibs/xml/sax/helpers/MyNamespaceSupport.java) extends 
`org.xml.sax.helpers.NamespaceSupport` to provide some handy features.

It is tedious to find prefix from `org.xml.sax.helpers.NamespaceSupport`. for example:

~~~java
import org.xml.sax.helpers.NamespaceSupport;

NamespaceSupport nsSupport = new NamespaceSupport();
nsSupport.declarePrefix("ns1", "http://mynamespace1");
nsSupport.declarePrefix("", "http://mynamespace2");

// to find prefix of "http://mynamespace1"
System.out.println(nsSupport.getPrefix("http://mynamespace1")); // prints "ns1"

// let us try to find prefix of "http://mynamespace2"
System.out.println(nsSupport.getPrefix("http://mynamespace2")); // prints null, but we expect ""
~~~

As per javadoc, `NamespaceSupport.getPrefix(...)` returns prefixes only for those namespaces which are bound to non-empty prefix.  
So in order to find prefix properly, we should do something like this:

``java
String prefix = nsSupport.getPrefix(uri);
if(prefix==null){
    if(nsSupport.getURI("").equals(uri))
        prefix = "";
}
System.out.println("prefix: "+prefix);<br>
~~~

`MyNamespaceSupport` provides handy method `findPrefix(uri)` for this:

~~~java
import jlibs.xml.sax.helper.MyNamespaceSupport;

MyNamespaceSupport nsSupport = new MyNamespaceSupport();
nsSupport.declarePrefix("", "http://mynamespace2");

// let us try to find prefix of "http://mynamespace2"
System.out.println(nsSupport.findPrefix("http://mynamespace2")); // prints ""
~~~

It also helps you in suggesting prefixes:

~~~java
String prefix = nsSupport.declarePrefix("http://www.google.com"); // we are asking to generate prefix
System.out.println(prefix); // prints "google"
~~~

You can also override the suggested prefix for any uri, as below:

~~~java
nsSupport.suggestPrefix("tns", "http://java.sun.com"); // we are just suggesting (not declaring)
....
....
String prefix = nsSupport.declare("http://java.sun.com");
System.out.println(prefix); // prints "tns"
~~~

## DefaultNamespaceContext ##

JAXP provides interface `org.xml.sax.helpers.NamespaceContext` interface, but doesn't provide any concrete implementation for this.  
This interface is used to pass namespace and prefix binding for xpath evaluation (see `javax.xml.xpath.XPath`)

[DefaultNamespaceContext](https://github.com/santhosh-tekuri/jlibs/blob/master/xml/src/main/java/jlibs/xml/DefaultNamespaceContext.java) implements this interface.

~~~java
DefaultNamespaceContext nsContext = new DefaultNamespaceContext();
nsContext.declarePrefix("ns1", "http://namespace1");
String prefix = nsContext.declarePrefix("http://www.google.com"); // prefix will be suggested to "google"

// now we can use nsContext for xpath evaluation<br>
~~~

You can also create DefaultNamespaceContext from MyNamespaceSupport:

~~~java
MyNamespaceSupport nsSupport = .....
......
DefaultNamespaceContext nsContext = new DefaultNamespaceContext(nsSupport);
~~~

## ClarkName ##

[ClarkName](https://github.com/santhosh-tekuri/jlibs/blob/master/xml/src/main/java/jlibs/xml/ClarkName.java) is String notation for `javax.xml.namespaces.QName`.

~~~java
import jlibs.xml.ClarkName;

String str1 = ClarkName.valueOf("htpp://namespace1", "elem1"); // str1 will be "{http://namespace1}elem1"
String str2 = ClarkName.valueOf("", "elem2"); // str2 will be "elem1"
~~~

This is compatible with `QName.toString()`  
you can get the namespace and local-name back:

~~~java
String clarkName = ClarkName.valueOf("htpp://namespace1", "elem1");
String str[] = ClarkName.split(clarkName);
System.out.println(str[0]); // prints "http://namespace1"
System.out.println(str[2]); // prints "elem1"
~~~

support you want to save location of particular element in xpath form, for example:

~~~
ns1:elem1/ns2:elem2/ns2:elem3
where
ns1="http://namespace1"
ns2="http://namespace2"
ns3="http://namespace3"
~~~

you can convert into raw xpath by inlining namespaces into xpath:

~~~
{http://namespace1}elem1/{http://namespace2}elem2/{http://namespace1}elem3
~~~

this form will be easier to save to some file and restore it back; ClarkName provides handy method to split such paths.  

~~~java
String clarkPath = "{http://namespace1}elem1/{http://namespace2}elem2/{http://namespace1}elem3";
String clarkNames[] = ClarkName.splitPath(clarkPath);
~~~


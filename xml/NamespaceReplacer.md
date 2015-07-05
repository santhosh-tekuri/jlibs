---
title: NamespaceReplacer
layout: default
---

# Replacing XML Namespaces #

[NamespaceReplacer](https://github.com/santhosh-tekuri/jlibs/blob/master/xml/src/main/java/jlibs/xml/sax/NamespaceReplacer.java) extends
[SAXDelegate](https://github.com/santhosh-tekuri/jlibs/blob/master/xml/src/main/java/jlibs/xml/sax/SAXDelegate.java).

`SAXDelegate` is used to chain sax handlers. It implements all sax handlers `ContentHandler`, `LexicalHandler` etc.  
and it has `setXXXHandler(…)` methods to support chaining.

First create a map specifying the namespaces to be replaced:

```java
Map<String, String> namespaces = new HashMap<String, String>();
namespaces.put("http://schemas.xmlsoap.org/soap/envelope/", "http://www.w3.org/2003/05/soap-envelope");
namespaces.put("http://jlibs.org", "");
```

here we are trying to replace:

- `http://schemas.xmlsoap.org/soap/envelope/` with `http://www.w3.org/2003/05/soap-envelope`
- `http://jlibs.org` with empty namespace

Now create `NamespaceReplacer`:

```java
import jlibs.xml.sax.NamespaceReplacer;

NamespaceReplacer nsReplacer = new NamespaceReplacer(namespaces);
```

let us say you have `MyDefaultHandler` which is supposed to recieve sax events.  
Rather than registering `MyDefaultHandler` with `SAXParser`, you register `nsReplacer`
with `SAXParser`, and `MyDefaultHandler` is registered with `nsReplacer`.

```java
import javax.xml.parsers.SAXParser;

MyDefaultHandler myHandler = new MyDefaultHandler();
nsReplacer.setContentHandler(myHandler)
SAXParser parser = createNamespaceAwareSAXParser();
parser.parse(xmlFile, nsReplacer);
```

Now sax events recieved by `myHandler` will have namespaces replaced.

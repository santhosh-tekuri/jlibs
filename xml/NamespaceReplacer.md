---
title: NamespaceReplacer
layout: default
---

# Replacing XML Namespaces #

[NamespaceReplacer](https://github.com/santhosh-tekuri/jlibs/blob/master/xml/src/main/java/jlibs/xml/sax/NamespaceReplacer.java) extends
`org.xml.sax.helpers.XMLFilterImpl`.

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

XMLReader reader = createNamespaceAwareSAXParser().getXMLReader();
reader = new NamespaceReplacer(reader, namespaces);
DefaultHandler myHandler = ...;
reader.parse(xmlFile, myHandler);
```

Now sax events recieved by `myHandler` will have namespaces replaced.

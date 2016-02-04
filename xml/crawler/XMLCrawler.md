---
title: Crawl XML(wsdl, xsd, xsl) documents
layout: default
---

# Crawl XML(wsdl, xsd, xsl) documents #

A [WebCrawler](http://en.wikipedia.org/wiki/Web_crawler) takes url of html document as input.   #
It parses the html document and finds the resources referred by <code>&lt;a href="..."&gt;</code> in that document.  
It repeats the same process on the html resources referred and so on.  
While doing this it saves the resources into local filesystem.

Similarly `jlibs.xml.sax.crawl.XMLCrawler` does the same for XML Files.

An xml document can refer to another xml document in many ways. for example:

- XMLSchema uses `<xsd:import>` and `<xsd:include>`
- WSDL uses `<wsdl:import>`, `<wsdl:include>`

i.e each type of xml document has its own way of referring other xml documents.

`XMLCrawler` is preconfigured to crawl xmlschema, wsdl and xsl documents.

~~~java
import jlibs.xml.sax.crawl.XMLCrawler;

String dir = "d:\\crawl"; // directory where to save crawled documents
String wsdl = "https://fps.amazonaws.com/doc/2007-01-08/AmazonFPS.wsdl"; // wsdl to be crawled

new XMLCrawler().crawlInto(new InputSource(wsdl), new File(dir));
~~~

After running above code, you will find following files in `d:\crawl` directory:

- AmazonFPS.wsdl
- AmazonFPS.xsd

It never overwrites any existing file in that directory. So if you run the above code twice, you will see following files in `d:\crawl` directory:

- AmazonFPS1.wsdl
- AmazonFPS1.xsd
- AmazonFPS.wsdl
- AmazonFPS.xsd


you could also explicitly specify the target wsdl file to be created:

~~~java
new XMLCrawler().crawl(new InputSource(wsdl), new File("d:\\crawl\\target.wsdl"));
~~~

in above code second argument is the file where to save the document specified in first argument.  
It will save all referred documents in the containing directory of second argument.  
for example, the above creates following files in `d:\crawl`:

- target.wsdl
- AmazonFPS.xsd


<b>NOTE:</b> All files are saved directly in given directory, i.e, no subdirectories are created.

## How to crawl new document type ##

`XMLCrawler` can be configured to crawl any type of xml using `CrawlingRules`.

The no-arg constructor uses `CrawlingRules` configured for wsdl, xsd and xsl documents.

Let us see how to configure XMLCrawler for XMLSchema Documents using `CrawlingRules`

~~~java
import jlibs.xml.sax.crawl.XMLCrawler;
import jlibs.xml.sax.crawl.CrawlingRules;
import jlibs.xml.Namespaces;

QName xsd_schema = new QName(Namespaces.URI_XSD, "schema");
QName xsd_import = new QName(Namespaces.URI_XSD, "import");
QName attr_schemaLocation = new QName("schemaLocation");
QName xsd_include = new QName(Namespaces.URI_XSD, "include");

CrawlingRules rules = new CrawlingRules();
rules.addExtension("xsd", xsd_schema);
rules.addAttributeLink(xsd_schema, xsd_import, attr_schemaLocation);
rules.addAttributeLink(xsd_schema, xsd_include, attr_schemaLocation);

XMLCrawler crawler = new XMLCrawler(rules);

// now crawler is ready for use
String xsd = "http://somesite.com/xsds/complex.xsd";
String dir = "d:\\crawl";
crawler.crawlInto(new InputSource(xsd), new File(dir));
~~~

First we need to tell, how to recognize the extension of xml file.

~~~java
rules.addExtension("xsd", xsd_schema);
~~~

the above line says that xml file with root element `{"http://www.w3.org/2001/XMLSchema"}schema` should be saved with file extension `xsd`.

~~~java
rules.addAttributeLink(xsd_schema, xsd_import, attr_schemaLocation);
rules.addAttributeLink(xsd_schema, xsd_include, attr_schemaLocation);
~~~

The above lines tell that `schemaLocation` attribute of `xsd:schema/xsd:import` and `xsd:schema/xsd:include` are used to refer other xml files.

## Customization ##

`CrawlerListener` interface can be used to customize crawling behavior. It has two methods:

~~~java
public boolean doCrawl(URL url);
public File toFile(URL url, String extension);
~~~

The default implementation used is `DefaultCrawlerListener`.

`doCrawl(url)` is used to determine whether given url should be crawled or not. `DefaultCrawlerListener` implementation always returns `true`.

`toFile(...)` is used to determine the file into which the specified url needs to be saved.

To use your implementation of `CrawlerListener`, you have to use following method in `XMLCrawler`

~~~java
public File crawl(InputSource document, CrawlerListener listener, File file) throws IOException
~~~

the last argument `file` can be null, if you don't want to specify target file.  
In such case, `listener.toFile(...)` is used to determine target file.

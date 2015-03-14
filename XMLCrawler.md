A [WebCrawler](http://en.wikipedia.org/wiki/Web_crawler) takes url of html document as input. <br>
It parses the html document and finds the resources referred by <code>&lt;a href="..."&gt;</code> in that document.  <br>
It repeats the same process on the html resources referred and so on.  <br>
While doing this it saves the resources into local filesystem.<br>
<br>
Similarly <code>jlibs.xml.sax.crawl.XMLCrawler</code> is for XML Files.<br>
<br>
However an xml document can refer to another xml document in many ways. for example:<br>
<br>
XMLSchema uses <code>&lt;xsd:import&gt;</code> and <code>&lt;xsd:include&gt;</code><br>
WSDL uses <code>&lt;wsdl:import</code>, <code>&lt;wsdl:include&gt;</code>, <code>&lt;xsd:import&gt;</code> and <code>&lt;xsd:include&gt;</code>

i.e each type of xml document has its own way of referring other xml documents.<br>
<br>
<code>XMLCrawler</code> is preconfigured to crawl xmlschema, wsdl and xsl documents.<br>
<br>
<b>Usage:</b>

<pre><code>import jlibs.xml.sax.crawl.XMLCrawler;<br>
<br>
String dir = "d:\\crawl"; // directory where to save crawled documents<br>
String wsdl = "https://fps.amazonaws.com/doc/2007-01-08/AmazonFPS.wsdl"; // wsdl to be crawled<br>
<br>
new XMLCrawler().crawlInto(new InputSource(wsdl), new File(dir));<br>
</code></pre>

All xml documents are saved into the specified directory. After running above code, you will find following files in <code>d:\crawl</code> directory<br>
<pre><code>AmazonFPS.wsdl<br>
AmazonFPS.xsd<br>
</code></pre>
It never overwrites any existing file in that directory. So if you run the above code twice, you will see following files in <code>d:\crawl</code> directory<br>
<pre><code>AmazonFPS1.wsdl<br>
AmazonFPS1.xsd<br>
AmazonFPS.wsdl<br>
AmazonFPS.xsd<br>
</code></pre>

you could also do:<br>
<pre><code>new XMLCrawler().crawl(new InputSource(wsdl), new File("d:\\crawl\\target.wsdl"));<br>
</code></pre>

<code>crawl(...)</code> method's second argument is the file where to save the document specified in first argument.  <br>
It will save all referred documents in the containing directory of second argument. <br>
for example, the above creates following files in <code>d:\crawl</code>
<pre><code>target.wsdl<br>
AmazonFPS.xsd<br>
</code></pre>

<b>NOTE:</b> All files are saved directly in given directory, i.e, no subdirectories are created.<br>
<hr />
<code>XMLCrawler</code> can be configured to crawl any type of xml using <code>CrawlingRules</code>.<br>
<br>
The no-arg constructor uses <code>CrawlingRules</code> configured for wsdl, xsd and xsl documents.<br>
<br>
Let us see how to configure XMLCrawler for XMLSchema Documents using <code>CrawlingRules</code>.<br>
<br>
<pre><code>import jlibs.xml.sax.crawl.XMLCrawler;<br>
import jlibs.xml.sax.crawl.CrawlingRules;<br>
import jlibs.xml.Namespaces;<br>
<br>
QName xsd_schema = new QName(Namespaces.URI_XSD, "schema");<br>
QName xsd_import = new QName(Namespaces.URI_XSD, "import");<br>
QName attr_schemaLocation = new QName("schemaLocation");<br>
QName xsd_include = new QName(Namespaces.URI_XSD, "include");<br>
<br>
CrawlingRules rules = new CrawlingRules();<br>
rules.addExtension("xsd", xsd_schema);<br>
rules.addAttributeLink(xsd_schema, xsd_import, attr_schemaLocation);<br>
rules.addAttributeLink(xsd_schema, xsd_include, attr_schemaLocation);<br>
<br>
XMLCrawler crawler = new XMLCrawler(rules);<br>
<br>
// now crawler is ready for use<br>
String xsd = "http://somesite.com/xsds/complex.xsd";<br>
String dir = "d:\\crawl";<br>
crawler.crawlInto(new InputSource(xsd), new File(dir));<br>
</code></pre>

First we need to tell, how to recognize the extension of xml file.<br>
<pre><code>rules.addExtension("xsd", xsd_schema);<br>
</code></pre>
here we are saying that xml file with root element <code>{"http://www.w3.org/2001/XMLSchema"}schema</code> should be saved with file extension <code>xsd</code>.<br>
<pre><code>rules.addAttributeLink(xsd_schema, xsd_import, attr_schemaLocation);<br>
rules.addAttributeLink(xsd_schema, xsd_include, attr_schemaLocation);<br>
</code></pre>
The above lines tell that <code>schemaLocation</code> attribute of <code>xsd:schema/xsd:import</code> and <code>xsd:schema/xsd:include</code> are used to refer other xml files.<br>
<hr />

<b>Customization:</b>

<code>CrawlerListener</code> interface can be used to customize crawling behavior. It has two methods:<br>
<pre><code>public boolean doCrawl(URL url);<br>
public File toFile(URL url, String extension);<br>
</code></pre>
The default implementation used is <code>DefaultCrawlerListener</code>.<br>
<br>
<code>doCrawl(url)</code> is used to determine whether given url should be crawled or not. <code>DefaultCrawlerListener</code> implementation always returns <code>true</code>.<br>
<br>
<code>toFile(...)</code> is used to determine the file into which the specified url needs to be saved.<br>
<br>
to use your implementation of <code>CrawlerListener</code>, you have to use following method in <code>XMLCrawler</code>
<pre><code>public File crawl(InputSource document, CrawlerListener listener, File file) throws IOException<br>
</code></pre>
the last argument <code>file</code> can be null, if you don't want to specify target file. <br>
In such case, <code>listener.toFile(...)</code> is used to determine target file.
**Getting Started**

Consider `Company` class containing array of `Employee`;

```
class Company{
    String name;
    Employee employees[];

    Company(String name, Employee... employees){
        this.name = name;
        this.employees = employees;
    }
}

class Employee{
    String id;
    String name;
    String email;
    int age;

    Employee(String id, String name, String email, int age){
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
```

To create xml:
```
import jlibs.xml.sax.XMLDocument;
import javax.xml.transform.stream.StreamResult;

XMLDocument xml = new XMLDocument(new StreamResult(System.out), false, 4, null);
xml.startDocument();{
    xml.startElement("company");{
        xml.addAttribute("name", company.name);
        for(Employee emp: company.employees){
            xml.startElement("employee");{
                xml.addAttribute("id", emp.id);
                xml.addAttribute("age", ""+emp.age);
                xml.addElement("name", emp.name);
                xml.addElement("email", emp.email);
            }
            xml.endElement("employee");
        }
    }
    xml.endElement("company");
}
xml.endDocument();
```

Running this prints following:

```
<?xml version="1.0" encoding="UTF-8"?>
<company name="MyCompany">
    <employee id="1" age="20">
        <name>scott</name>
        <email>scott@gmail.com</email>
    </employee>
    <employee id="2" age="25">
        <name>alice</name>
        <email>alice@gmail.com</email>
    </employee>
</company>
```

The constructor of XMLDocument is:
```
XMLDocument(Result result, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerConfigurationException
```

The first argument is of type `javax.xml.transform.Result`; So we can even use `DOMResult` to create DOM;<br>
if last argument <code>encoding</code> is null, then it defaults to default XML encoding(<code>UTF-8</code>);<br>
<hr />
<b>NULL Friendly</b>

The methods to fire SAX events are <code>null</code> friendly. it means:<br>
<pre><code>xml.addAttribute("id", emp.id);<br>
</code></pre>

will not add attribute if <code>emp.id=null</code>. So you no longer need to write as below:<br>
<pre><code>if(emp.id!=null)<br>
    xml.addAttribute("id", emp.id);<br>
</code></pre>

<code>null</code> friendly methods, avoid code clutter and make it more readable.<br>
<br>
<b>Method Chaining</b>

The methods to fire SAX events return <code>this</code>. So method calls can be chained to produce more readable code<br>
<pre><code>xml.startElement("employee")<br>
        .addAttribute("id", emp.id)<br>
        .addAttribute("age", ""+emp.age);<br>
</code></pre>
instead of<br>
<pre><code>xml.startElement("employee");<br>
xml.addAttribute("id", emp.id);<br>
xml.addAttribute("age", ""+emp.age);<br>
</code></pre>

<b>Simple Text Only Elements</b>

You can do following:<br>
<pre><code>xml.addElement("email", emp.email);<br>
</code></pre>
instead of<br>
<pre><code>if(emp.email!=null){<br>
    xml.startElement("email");<br>
    xml.addText(emp.email);<br>
    xml.endElement("email");<br>
}<br>
</code></pre>

there is also <code>addCDATAElement(...)</code> available<br>
<br>
<b>End Element</b>

To end element, we do:<br>
<pre><code>xml.endElement("employee");<br>
</code></pre>

If you mis-spell element name here, it will throw <code>SAXException</code>:<br>
<blockquote><code>org.xml.sax.SAXException: expected &lt;/employee&gt;</code></blockquote>

there is also another variation of <code>endElement</code> with no arguments;<br>
<pre><code>xml.endElement();<br>
</code></pre>
This will implicitly find the recent element started and ends it.<br>
<br>
suppose we have:<br>
<pre><code>xml.endElement("elem3");<br>
xml.endElement("elem2");<br>
xml.endElement("elem1");<br>
</code></pre>
the same can be done in single line as below:<br>
<pre><code>xml.endElements("elem1");<br>
</code></pre>
This will do endElement() until <code>elem1</code> is closed;<br>
<br>
To end all elements started, do:<br>
<pre><code>xml.endElements();<br>
</code></pre>

<b>NOTE:</b>

<ul><li><code>endElements()</code> will do nothing if all elements are already closed;<br>
</li><li><code>endElements()</code> is implictly called in <code>endDocument()</code>. So you can safely ignore trailing end elements of xml</li></ul>

<b>DTD</b>
<pre><code>xml.addSystemDTD("company", "company.dtd");<br>
</code></pre>
will produce<br>

<code>&lt;!DOCTYPE company SYSTEM "company.dtd"&gt;</code>


<pre><code>xml.addPublicDTD("company", "-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");<br>
</code></pre>
will produce<br>
<br>
<code>&lt;!DOCTYPE company PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"&gt;</code>

<b>Adding XML</b>

<pre><code>xml.startElement("elem1");<br>
xml.addXML("&lt;test&gt;&lt;test1&gt;first&lt;/test1&gt;&lt;test2&gt;second&lt;/test2&gt;&lt;/test&gt;", false);<br>
xml.endElement();<br>
</code></pre>
will produce:<br>
<br>
<pre><code>&lt;elem1&gt;<br>
    &lt;test&gt;<br>
        &lt;test1&gt;first&lt;/test1&gt;<br>
        &lt;test2&gt;second&lt;/test2&gt;<br>
    &lt;/test&gt;<br>
&lt;/elem1&gt;<br>
</code></pre>

The first argument to <code>addXML(...)</code> should be well-formed xml string;<br>
second argument will tell whether to ignore root element or not;<br>
when used <code>true</code> in above sample it will produce:<br>
<pre><code>&lt;elem1&gt;<br>
    &lt;test1&gt;first&lt;/test1&gt;<br>
    &lt;test2&gt;second&lt;/test2&gt;<br>
&lt;/elem1&gt;<br>
</code></pre>

there are following other variations of addXML(...) available:<br>
<pre><code>public XMLDocument addXML(InputSource is, boolean excludeRoot) throws SAXException<br>
</code></pre>

for example, you could write:<br>
<pre><code>xml.addXML(new InputSource("notes.xml"), true);<br>
</code></pre>

<b>Miscellaneous</b>
<pre><code>xml.addComment("this is comment");<br>
xml.addCDATA("this is inside cdata");<br>
<br>
// to produce: &lt;?xml-stylesheet href="classic.xsl" type="text/xml"?&gt;<br>
xml.addProcessingInstruction("xml-stylesheet", "href=\"classic.xsl\" type=\"text/xml\"");<br>
</code></pre>
<hr />
<b>Namespaces</b>

<pre><code>static final String URI_JLIBS = "http://jlibs.org";<br>
static final String URI_COMP = "http://mycompany.com";<br>
static final String URI_EMP = "http://employee.com";<br>
<br>
xml.startDocument();<br>
xml.startElement(URI_COMP, "company")<br>
        .addAttribute("name", "mycompany")<br>
        .addAttribute(URI_JLIBS, "version", "0.1")<br>
        .startElement(URI_EMP, "employee")<br>
            .addAttribute("name", "scott")<br>
            .addElement(URI_EMP, "email", "scott@google.com")<br>
        .endElement()<br>
    .endElement();<br>
xml.endDocument();<br>
</code></pre>
will produce the following:<br>
<pre><code>&lt;?xml version="1.0" encoding="UTF-8"?&gt;<br>
&lt;mycompany:company xmlns:mycompany="http://mycompany.com" name="mycompany" jlibs:version="0.1" xmlns:jlibs="http://jlibs.org"&gt;<br>
    &lt;employee:employee xmlns:employee="http://employee.com" name="scott"&gt;<br>
        &lt;employee:email&gt;scott@google.com&lt;/employee:email&gt;<br>
    &lt;/employee:employee&gt;<br>
&lt;/mycompany:company&gt;<br>
</code></pre>

You can notice that, we didn't tell what prefix to use.<br>
<code>XMLDocument</code> is intelligent enough to generate prefixes automatically from namespace uri.<br>
<br>
<b>Standard Namespaces</b>

<code>jlibs.xml.Namespaces</code> class contains most frequently used namespaces like:<br>
<pre><code>public static final String URI_XSD   = "http://www.w3.org/2001/XMLSchema";<br>
public static final String URI_XSI   = "http://www.w3.org/2001/XMLSchema-instance";<br>
public static final String URI_XSL   = "http://www.w3.org/1999/XSL/Transform";<br>
</code></pre>

<code>Namespaces.suggestPrefix(String uri)</code> suggests most commonly used prefix for any of these standard prefixes;<br>
<pre><code>String prefix = Namespaces.suggestPrefix(Namespaces.URI_XSD); // prefix will be "xsd"<br>
</code></pre>

<code>XMLDocument</code> uses suggested prefixes from <code>Namespaces</code> if available; For example:<br>
<pre><code>import static jlibs.xml.Namespaces.*;<br>
<br>
xml.startDocument();<br>
xml.startElement(URI_XSD, "element")<br>
    .addAttribute("name", "employee")<br>
    .addAttribute("type", "employeeType");<br>
xml.endDocument();<br>
</code></pre>
will produce the following:<br>
<pre><code>&lt;xsd:element xmlns:xsd="http://www.w3.org/2001/XMLSchema" name="employee" type="employeeType"/&gt;<br>
</code></pre>

<b>Suggesting Prefixes</b>

<pre><code>public void suggestPrefix(String prefix, String uri)<br>
</code></pre>

this method can be used to suggest prefix for given uri;<br>
Note that, using this method you can even ovverride the prefixes for standard namespaces, if needed.<br>
<br>
<pre><code>xml.startDocument();<br>
xml.suggestPrefix(URI_JLIBS, "jlibs");<br>
xml.suggestPrefix(URI_COMP, "comp");<br>
xml.suggestPrefix(URI_EMP, "emp");<br>
<br>
xml.startElement(URI_COMP, "company")<br>
        .addAttribute("name", "mycompany")<br>
        .addAttribute(URI_JLIBS, "version", "0.1")<br>
        .startElement(URI_EMP, "employee")<br>
            .addAttribute("name", "scott")<br>
            .addElement(URI_EMP, "email", "scott@google.com")<br>
        .endElement()<br>
    .endElement();<br>
xml.endDocument();<br>
</code></pre>
will produce the following:<br>
<pre><code>&lt;comp:company xmlns:comp="http://mycompany.com" name="mycompany" jlibs:version="0.1" xmlns:jlibs="http://jlibs.org"&gt;<br>
    &lt;emp:employee xmlns:emp="http://employee.com" name="scott"&gt;<br>
        &lt;emp:email&gt;scott@google.com&lt;/emp:email&gt;<br>
    &lt;/emp:employee&gt;<br>
&lt;/comp:company&gt;<br>
</code></pre>

<b>Declaring Prefixes</b>

When you declare prefix, xmlns attribute will be added to generated xml;<br>
This could be handy in following situation:<br>
<br>
<pre><code>xml.startDocument();<br>
xml.startElement(URI_COMP, "company")<br>
        .startElement(URI_EMP, "employee")<br>
            .addAttribute("name", "scott")<br>
        .endElement()<br>
        .startElement(URI_EMP, "employee")<br>
            .addAttribute("name", "alice")<br>
        .endElement()<br>
        .startElement(URI_EMP, "employee")<br>
            .addAttribute("name", "alean")<br>
        .endElement()<br>
   .endElement();<br>
xml.endDocument();    <br>
</code></pre>
produces the following:<br>
<pre><code>&lt;mycompany:company xmlns:mycompany="http://mycompany.com"&gt;<br>
    &lt;employee:employee xmlns:employee="http://employee.com" name="scott"/&gt;<br>
    &lt;employee:employee xmlns:employee="http://employee.com" name="alice"/&gt;<br>
    &lt;employee:employee xmlns:employee="http://employee.com" name="alean"/&gt;<br>
&lt;/mycompany:company&gt;<br>
</code></pre>

In output, you can notice that <code>employee</code> namespace is declared in each <code>&lt;employee&gt;</code> element.<br>
The xml is looking cluttered because of this. If we could have defined <code>employee</code> namespace in <code>&lt;company&gt;</code>, it would be better.<br>

To do this:<br>
<pre><code>xml.startDocument();<br>
xml.declarePrefix(URI_EMP); // we are declaring manually here<br>
<br>
xml.startElement(URI_COMP, "company")<br>
        .startElement(URI_EMP, "employee")<br>
            .addAttribute("name", "scott")<br>
        .endElement()<br>
        .startElement(URI_EMP, "employee")<br>
            .addAttribute("name", "alice")<br>
        .endElement()<br>
        .startElement(URI_EMP, "employee")<br>
            .addAttribute("name", "alean")<br>
        .endElement()<br>
   .endElement(); <br>
xml.endDocument();   <br>
</code></pre>
now the above code produces:<br>
<pre><code>&lt;mycompany:company xmlns:mycompany="http://mycompany.com" xmlns:employee="http://employee.com"&gt;<br>
    &lt;employee:employee name="scott"/&gt;<br>
    &lt;employee:employee name="alice"/&gt;<br>
    &lt;employee:employee name="alean"/&gt;<br>
&lt;/mycompany:company&gt;<br>
</code></pre>
notice that <code>xmlns:employee</code> attribute is now moved to <code>&lt;mycompany&gt;</code> element.<br>
<br>
there is also another variant of <code>declarePrefix(...)</code>
<pre><code>public boolean declarePrefix(String prefix, String uri)<br>
</code></pre>
using this, you can specify prefix of your wish.<br>
<br>
<b>Computing QNames</b>

<pre><code>xml.startDocument();<br>
xml.declarePrefix("emp", URI_EMP);<br>
<br>
xml.startElement(URI_XSD, "schema");<br>
        .startElement(URI_XSD, "element")<br>
            .addAttribute("name", "employee")<br>
            .addAttribute("type", toQName(URI_EMP, "emloyeeType"));<br>
xml.endDocument();<br>
</code></pre>
will produce following:<br>
<pre><code>&lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:emp="http://employee.com"&gt;<br>
    &lt;xsd:element name="employee" type="emp:emloyeeType"/&gt;<br>
&lt;/xsd:schema&gt;<br>
</code></pre>

here the value of <code>@type</code> is a qname which should be valid. i.e you want to use correct prefix<br>
<code>toQName(uri, localPart)</code> will return the correct qname string.<br>
if the given uri is not yet declared, it will be declared automatically.<br>
<hr />
<b>Mark and Release</b>

let us say we have following two methods:<br>
<pre><code>public static void serializeCompany(XMLDocument xml, Company company) throws SAXException{<br>
    xml.startElement("company");<br>
    xml.addAttribute("name", company.name);<br>
    for(Employee emp: company.employees){<br>
        serializeEmployee(xml, emp);<br>
    }<br>
    xml.endElement("company");<br>
}<br>
<br>
public static void serializeEmployee(XMLDocument xml, Employee emp) throws SAXException{<br>
    xml.startElement("employee");{<br>
        xml.addAttribute("id", emp.id);<br>
        xml.addAttribute("age", ""+emp.age);<br>
        xml.addElement("name", emp.name);<br>
        xml.addElement("email", emp.email);<br>
    }<br>
    xml.endElement();<br>
//    xml.endElement();<br>
}<br>
<br>
public static void main(String[] args) throws Exception{<br>
    Company company = createCompany();<br>
    XMLDocument xml = new XMLDocument(new StreamResult(System.out), false, 4, null);<br>
    xml.startDocument();<br>
    serializeCompany(xml, company);<br>
    xml.endDocument();<br>
}<br>
</code></pre>

when you uncomment last line in <code>serializeEmployee(...)</code> it produces following exception:<br>
<pre><code>Exception in thread "main" org.xml.sax.SAXException: can't find matching start element<br>
	at jlibs.xml.sax.XMLDocument.findEndElement(XMLDocument.java:244)<br>
	at jlibs.xml.sax.XMLDocument.endElement(XMLDocument.java:257)<br>
	at jlibs.xml.sax.XMLDocument.endElement(XMLDocument.java:264)<br>
	at Example.serializeCompany(XMLDocument.java:483)<br>
	at Example.main(XMLDocument.java:504)<br>
</code></pre>
from above stacktrace, you will notice that the error is reported for <code>serializeCompany(...)</code>;<br>
but actually the bug is in <code>serializeEmployee(...)</code> method.<br>
<br>
now change <code>serializeCompany(...)</code> to use marking support as follows:<br>
<pre><code>public static void serializeCompany(XMLDocument xml, Company company) throws SAXException{<br>
    xml.startElement("company");<br>
    xml.addAttribute("name", company.name);<br>
    for(Employee emp: company.employees){<br>
        xml.mark();<br>
        serializeEmployee(xml, emp);<br>
        xml.release();<br>
    }<br>
    xml.endElement("company");<br>
}<br>
</code></pre>
now the exception produced will be:<br>
<pre><code>Exception in thread "main" org.xml.sax.SAXException: can't find matching start element<br>
	at jlibs.xml.sax.XMLDocument.findEndElement(XMLDocument.java:244)<br>
	at jlibs.xml.sax.XMLDocument.endElement(XMLDocument.java:268)<br>
	at Example.serializeEmployee(XMLDocument.java:496)<br>
	at Example.serializeCompany(XMLDocument.java:482)<br>
	at Example.main(XMLDocument.java:506)<br>
</code></pre>
i.e the stacktrace now clearly tells the bug is in <code>serializeEmployee(...)</code> method;<br>
<br>
let us see marking support in detail:<br>
<pre><code>xml.startElement("elem1");<br>
...<br>
xml.startElement("elem2");<br>
....<br>
xml.mark();<br>
xml.startElement("elem3");<br>
....<br>
xml.startElement("elem4");<br>
.....<br>
xml.release(); // will close elem4 and elem3 i.e upto the mark and clears the mark<br>
xml.endElement("elem2");<br>
</code></pre>

xml.release() must be called prior to ending Elements before the mark. i.e,<br>
<pre><code>xml.startElement("elem1");<br>
...<br>
xml.mark();<br>
....<br>
xml.endElement("elem1"); // will throw SAXException: can't find matching start element<br>
</code></pre>

<b>NOTE:</b>

<ul><li>You can mark as many times as possible. i.e, multiple marks can exist;<br>
</li><li>endElements() will only end elements which are started after recent mark.<br>
</li><li><code>release()</code> implictly does end elements</li></ul>

you can also release any mark, instead of last mark as below:<br>
<pre><code>int mark = mark();<br>
...<br>
xml.mark();<br>
...<br>
xml.mark();<br>
...<br>
xml.release(mark);<br>
</code></pre>

when you call <code>mark()</code>, it returns the number of mark;<br>
first call to <code>mark()</code> returns 1. next call to <code>mark()</code> will return 2, if earlier mark is not released;<br>
<br>
NOTE:<br>
there is an implicit mark 0, which should not be released by user. it is used by <code>XMLDocument</code>;<br>
<hr />

You can create wrappers for <code>XMLDocument</code> to make creating specific type of xml document easier;<br>
<br>
JLibs has one such wrapper <code>jlibs.xml.xsd.XSDocument</code>; this lets us make XMLSchema documents easier;<br>
<pre><code>import jlibs.xml.xsd.XSDocument;<br>
<br>
XSDocument xsd = new XSDocument(new StreamResult(System.out), false, 4, null);<br>
xsd.startDocument();<br>
{<br>
    String n1 = "http://www.example.com/N1";<br>
    String n2 = "http://www.example.com/N2";<br>
    xsd.xml().declarePrefix("n1", n1);<br>
    xsd.xml().declarePrefix("n2", n2);<br>
    xsd.startSchema(n1);<br>
    {<br>
        xsd.addImport(n2, "imports/b.xsd");<br>
        xsd.startComplexType().name("MyType");<br>
        {<br>
            xsd.startCompositor(Compositor.SEQUENCE);<br>
            xsd.startElement().ref(n1, "e1").endElement();<br>
            xsd.endCompositor();<br>
        }<br>
        xsd.endComplexType();<br>
        xsd.startElement().name("root").type(n1, "MyType").endElement();<br>
    }<br>
    xsd.endSchema();<br>
}<br>
xsd.endDocument();<br>
</code></pre>

produces following output:<br>
<pre><code>&lt;xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" targetNamespace="http://www.example.com/N1" xmlns:n1="http://www.example.com/N1" xmlns:n2="http://www.example.com/N2"&gt;<br>
    &lt;xsd:import namespace="http://www.example.com/N2" schemaLocation="imports/b.xsd"/&gt;<br>
    &lt;xsd:complexType name="MyType"&gt;<br>
        &lt;xsd:sequence&gt;<br>
            &lt;xsd:element ref="n1:e1"/&gt;<br>
        &lt;/xsd:sequence&gt;<br>
    &lt;/xsd:complexType&gt;<br>
    &lt;xsd:element name="root" type="n1:MyType"/&gt;<br>
&lt;/xsd:schema&gt;<br>
</code></pre>

You can also create similar wrappers.<br>
<hr />

<b>ObjectInputSource:</b>

This is an extension of <code>org.xml.sax.InputSource</code> with single abstract method:<br>
<pre><code>protected abstract void write(E obj) throws SAXException;<br>
</code></pre>

<code>InputSource</code> wraps <code>systemID</code> or <code>OutputStream</code> or <code>Reader</code> which is source of xml;<br>
Similarly, <code>ObjectInputSource</code> wraps a java object, which is the source of xml:<br>
<pre><code>new ObjectInputSource&lt;E&gt;(E obj, XMLDocument xml)<br>
</code></pre>

It is job of its subclass to override <code>write(E obj, XMLDocument xml)</code> and fire SAX events.<br>
<br>
Let us write an implementation of <code>ObjectInputSource</code> for <code>Company</code>;<br>
<pre><code>import jlibs.xml.sax.ObjectInputSource;<br>
import org.xml.sax.SAXException;<br>
<br>
class CompanyInputSource extends ObjectInputSource&lt;Company&gt;{<br>
    public CompanyInputSource(Company company){<br>
        super(company);<br>
    }<br>
<br>
    @Override<br>
    protected void write(Company company, XMLDocument xml) throws SAXException{<br>
        xml.startElement("company");<br>
        xml.addAttribute("name", company.name);<br>
        for(Employee emp: company.employees){<br>
            xml.startElement("employee");{<br>
                xml.addAttribute("id", emp.id);<br>
                xml.addAttribute("age", ""+emp.age);<br>
                xml.addElement("name", emp.name);<br>
                xml.addElement("email", emp.email);<br>
            }<br>
            xml.endElement("employee");<br>
        }<br>
        xml.endElement("company");<br>
    }<br>
}<br>
</code></pre>

Note that <code>xml.startDocument()</code> is implicitly called before <code>write(...)</code> method and<br>
<code>xml.endDocument()</code> is called implicitly after <code>write(...)</code> method;<br>
<br>
To create XML, now we can do the following:<br>
<br>
<pre><code>import javax.xml.transform.TransformerException;<br>
<br>
public static void main(String[] args) throws TransformerException, XMLStreamException{<br>
    Employee scott = new Employee("1", "scott", "scott@gmail.com", 20);<br>
    Employee alice = new Employee("2", "alice", "alice@gmail.com", 25);<br>
    Company company = new Company("MyCompany", scott, alice);<br>
<br>
    // print company to System.out as xml<br>
    new CompanyInputSource(company).writeTo(System.out, false, 4, null);<br>
}<br>
</code></pre>

<code>ObjectInputSource</code> contains several methods to serialize the SAX events:<br>
<pre><code>public void writeTo(Writer writer, boolean omitXMLDeclaration, int indentAmount) throws TransformerException<br>
public void writeTo(OutputStream out, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerException<br>
public void writeTo(String systemID, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerException<br>
</code></pre>

if <code>encoding</code> is null, then it defaults to default XML encoding(<code>UTF-8</code>);<br>
These <code>writeTo(...)</code> methods use Identity Trasformer;<br>
<br>
Your comments are appreciated;
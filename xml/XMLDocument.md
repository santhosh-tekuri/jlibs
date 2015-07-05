---
title: XMLDocument
layout: default
---

# Creating XML using SAX 

Consider `Company` class containing array of `Employee`;

```java
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

```java
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

```xml
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

```java
XMLDocument(Result result, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerConfigurationException
```

The first argument is of type `javax.xml.transform.Result`; So we can even use `DOMResult` to create DOM;  
if last argument `encoding` is null, then it defaults to default XML encoding(`UTF-8`);

---

## NULL Friendly ##

The methods to fire SAX events are `null` friendly. it means:

```java
xml.addAttribute("id", emp.id);
```

will not add attribute if `emp.id=null`. So you no longer need to write as below:

```java
if(emp.id!=null)
    xml.addAttribute("id", emp.id);
```

`null` friendly methods, avoid code clutter and make it more readable

## Method Chaining ##

The methods to fire SAX events return `this`. So method calls can be chained to produce more readable code

```java
xml.startElement("employee")
        .addAttribute("id", emp.id)
        .addAttribute("age", ""+emp.age);
```

instead of:

```java
xml.startElement("employee");
xml.addAttribute("id", emp.id);
xml.addAttribute("age", ""+emp.age);
```

## Simple Text Only Elements ##

You can do following:

```java
xml.addElement("email", emp.email);
```

instead of:

```java
if(emp.email!=null){
    xml.startElement("email");
    xml.addText(emp.email);
    xml.endElement("email");
}
```

there is also `addCDATAElement(...)` available

## End Element ##

To end element, we do:

```java
xml.endElement("employee");
```

If you mis-spell element name here, it will throw `SAXException`:

```
org.xml.sax.SAXException: expected </employee>
```

there is also another variation of `endElement` with no arguments;

```java
xml.endElement();
```

This will implicitly find the recent element started and ends it.

suppose we have series of `endElement` calls as below:

```java
xml.endElement("elem3");
xml.endElement("elem2");
xml.endElement("elem1");
```

the same can be done in single line as below:

```java
xml.endElements("elem1");
```

This will do endElement() until `elem1` is closed

To end all elements started, do:

```java
xml.endElements();
```

**NOTE:**

- `endElements()` will do nothing if all elements are already closed
- `endElements()` is implictly called in `endDocument()`. So you can safely ignore trailing end elements of xml

## DTD ##

```java
xml.addSystemDTD("company", "company.dtd");
```

will produce:

```xml
<!DOCTYPE company SYSTEM "company.dtd">
```

```java
xml.addPublicDTD("company", "-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
```

will produce:

```xml
<!DOCTYPE company PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
```

## Adding XML ##

```java
xml.startElement("elem1");
xml.addXML("<test><test1>first</test1><test2>second</test2></test>", false);
xml.endElement();
```

will produce:

```xml
<elem1>
    <test>
        <test1>first</test1>
        <test2>second</test2>
    </test>
</elem1>
```

The first argument to `addXML(...)` should be well-formed xml string;  
second argument will tell whether to ignore root element or not;  

if second argument is `true` in above sample it will produce:  

```xml
<elem1>
    <test1>first</test1>
    <test2>second</test2>
</elem1>
```

there is another variation of addXML(...) available:

```java
public XMLDocument addXML(InputSource is, boolean excludeRoot) throws SAXException
```

for example, you could write:

```java
xml.addXML(new InputSource("notes.xml"), true);
```

## Miscellaneous ##

```java
xml.addComment("this is comment");
xml.addCDATA("this is inside cdata");

// to produce: <?xml-stylesheet href="classic.xsl" type="text/xml"?>
xml.addProcessingInstruction("xml-stylesheet", "href=\"classic.xsl\" type=\"text/xml\"");
```

## Namespaces ##

```java
static final String URI_JLIBS = "http://jlibs.org";
static final String URI_COMP = "http://mycompany.com";
static final String URI_EMP = "http://employee.com";

xml.startDocument();
xml.startElement(URI_COMP, "company")
        .addAttribute("name", "mycompany")
        .addAttribute(URI_JLIBS, "version", "0.1")
        .startElement(URI_EMP, "employee")
            .addAttribute("name", "scott")
            .addElement(URI_EMP, "email", "scott@google.com")
        .endElement()
    .endElement();
xml.endDocument();
```

will produce the following:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<mycompany:company xmlns:mycompany="http://mycompany.com" name="mycompany" jlibs:version="0.1" xmlns:jlibs="http://jlibs.org">
    <employee:employee xmlns:employee="http://employee.com" name="scott">
        <employee:email>scott@google.com</employee:email>
    </employee:employee>
</mycompany:company>
```

You can notice that, we didn't tell what prefix to use.  
`XMLDocument` is intelligent enough to generate prefixes automatically from namespace uri.

## Standard Namespaces ##

`jlibs.xml.Namespaces` class contains most frequently used namespaces like:

```java
public static final String URI_XSD   = "http://www.w3.org/2001/XMLSchema";
public static final String URI_XSI   = "http://www.w3.org/2001/XMLSchema-instance";
public static final String URI_XSL   = "http://www.w3.org/1999/XSL/Transform";
```

`Namespaces.suggestPrefix(String uri)` suggests most commonly used prefix for any of these standard prefixes

```java
String prefix = Namespaces.suggestPrefix(Namespaces.URI_XSD); // prefix will be "xsd"
```

`XMLDocument` uses suggested prefixes from `Namespaces` if available; For example:

```java
import static jlibs.xml.Namespaces.*;

xml.startDocument();
xml.startElement(URI_XSD, "element")
    .addAttribute("name", "employee")
    .addAttribute("type", "employeeType");
xml.endDocument();
```

will produce the following:

```xml
<xsd:element xmlns:xsd="http://www.w3.org/2001/XMLSchema" name="employee" type="employeeType"/>
```

## Suggesting Prefixes ##

```java
public void suggestPrefix(String prefix, String uri)
```

this method can be used to suggest prefix for given uri.  
Note that, using this method you can even ovverride the prefixes for standard namespaces, if needed.

```java
xml.startDocument();
xml.suggestPrefix(URI_JLIBS, "jlibs");
xml.suggestPrefix(URI_COMP, "comp");
xml.suggestPrefix(URI_EMP, "emp");

xml.startElement(URI_COMP, "company")
        .addAttribute("name", "mycompany")
        .addAttribute(URI_JLIBS, "version", "0.1")
        .startElement(URI_EMP, "employee")
            .addAttribute("name", "scott")
            .addElement(URI_EMP, "email", "scott@google.com")
        .endElement()
    .endElement();
xml.endDocument();
```

will produce the following:

```xml
<comp:company xmlns:comp="http://mycompany.com" name="mycompany" jlibs:version="0.1" xmlns:jlibs="http://jlibs.org">
    <emp:employee xmlns:emp="http://employee.com" name="scott">
        <emp:email>scott@google.com</emp:email>
    </emp:employee>
</comp:company>
```

## Declaring Prefixes ##

When you declare prefix, xmlns attribute will be added to generated xml.  
This could be handy in following situation:

```java
xml.startDocument();
xml.startElement(URI_COMP, "company")
        .startElement(URI_EMP, "employee")
            .addAttribute("name", "scott")
        .endElement()
        .startElement(URI_EMP, "employee")
            .addAttribute("name", "alice")
        .endElement()
        .startElement(URI_EMP, "employee")
            .addAttribute("name", "alean")
        .endElement()
   .endElement();
xml.endDocument();    
```

produces the following:

```xml
<mycompany:company xmlns:mycompany="http://mycompany.com">
    <employee:employee xmlns:employee="http://employee.com" name="scott"/>
    <employee:employee xmlns:employee="http://employee.com" name="alice"/>
    <employee:employee xmlns:employee="http://employee.com" name="alean"/>
</mycompany:company>
```

In output, you can notice that `employee` namespace is declared in each `<employee>` element.  
The xml is looking cluttered because of this. If we could have defined `employee` namespace in `<company>`, it would be better.

To do this:

```java
xml.startDocument();
xml.declarePrefix(URI_EMP); // we are declaring manually here

xml.startElement(URI_COMP, "company")
        .startElement(URI_EMP, "employee")
            .addAttribute("name", "scott")
        .endElement()
        .startElement(URI_EMP, "employee")
            .addAttribute("name", "alice")
        .endElement()
        .startElement(URI_EMP, "employee")
            .addAttribute("name", "alean")
        .endElement()
   .endElement(); 
xml.endDocument();   
```

now the above code produces:

```xml
<mycompany:company xmlns:mycompany="http://mycompany.com" xmlns:employee="http://employee.com">
    <employee:employee name="scott"/>
    <employee:employee name="alice"/>
    <employee:employee name="alean"/>
</mycompany:company>
```

notice that `xmlns:employee` attribute is now moved to `<mycompany>` element.

there is also another variant of `declarePrefix(...)`

```java
public boolean declarePrefix(String prefix, String uri)
```

using this, you can specify prefix of your wish.

## Computing QNames ##

```java
xml.startDocument();
xml.declarePrefix("emp", URI_EMP);

xml.startElement(URI_XSD, "schema");
        .startElement(URI_XSD, "element")
            .addAttribute("name", "employee")
            .addAttribute("type", toQName(URI_EMP, "emloyeeType"));
xml.endDocument();
```

will produce following:

```xml
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:emp="http://employee.com">
    <xsd:element name="employee" type="emp:emloyeeType"/>
</xsd:schema>
```

here the value of `@type` is a qname which should be valid. i.e you want to use correct prefix  
`toQName(uri, localPart)` will return the correct qname string.  
if the given uri is not yet declared, it will be declared automatically.

## Mark and Release ##

let us say we have following two methods:

```java
public static void serializeCompany(XMLDocument xml, Company company) throws SAXException{
    xml.startElement("company");
    xml.addAttribute("name", company.name);
    for(Employee emp: company.employees){
        serializeEmployee(xml, emp);
    }
    xml.endElement("company");
}

public static void serializeEmployee(XMLDocument xml, Employee emp) throws SAXException{
    xml.startElement("employee");{
        xml.addAttribute("id", emp.id);
        xml.addAttribute("age", ""+emp.age);
        xml.addElement("name", emp.name);
        xml.addElement("email", emp.email);
    }
    xml.endElement();
    //xml.endElement();
}

public static void main(String[] args) throws Exception{
    Company company = createCompany();
    XMLDocument xml = new XMLDocument(new StreamResult(System.out), false, 4, null);
    xml.startDocument();
    serializeCompany(xml, company);
    xml.endDocument();
}
```

The above code works perfectly.  
Now uncomment last line in `serializeEmployee(...)`. when you run, it produces following exception:

```
Exception in thread "main" org.xml.sax.SAXException: can't find matching start element
	at jlibs.xml.sax.XMLDocument.findEndElement(XMLDocument.java:244)
	at jlibs.xml.sax.XMLDocument.endElement(XMLDocument.java:257)
	at jlibs.xml.sax.XMLDocument.endElement(XMLDocument.java:264)
	at Example.serializeCompany(XMLDocument.java:483)
	at Example.main(XMLDocument.java:504)
```

from above stacktrace, you will notice that the error is reported for in `serializeCompany(...)`.  
but actually the bug is in `serializeEmployee(...)` method. This make finding bug tedious.

to make finding bug easier, change `serializeCompany(...)` to use marking support as follows:

```java
public static void serializeCompany(XMLDocument xml, Company company) throws SAXException{
    xml.startElement("company");
    xml.addAttribute("name", company.name);
    for(Employee emp: company.employees){
        xml.mark();
        serializeEmployee(xml, emp);
        xml.release();
    }
    xml.endElement("company");
}
```

now the exception produced will be:

```
Exception in thread "main" org.xml.sax.SAXException: can't find matching start element
	at jlibs.xml.sax.XMLDocument.findEndElement(XMLDocument.java:244)
	at jlibs.xml.sax.XMLDocument.endElement(XMLDocument.java:268)
	at Example.serializeEmployee(XMLDocument.java:496)
	at Example.serializeCompany(XMLDocument.java:482)
	at Example.main(XMLDocument.java:506)
```

i.e the stacktrace now clearly tells the bug is in `serializeEmployee(...)` method;
Thus the xml generated between mark and release is validated as complete element.

let us see marking support in detail:

```java
xml.startElement("elem1");
...
xml.startElement("elem2");
....
xml.mark();
xml.startElement("elem3");
....
xml.startElement("elem4");
.....
xml.release(); // will close elem4 and elem3 i.e upto the mark and clears the mark
xml.endElement("elem2");
```

`xml.release()` must be called prior to ending Elements before the mark. i.e,

```java
xml.startElement("elem1");
...
xml.mark();
....
xml.endElement("elem1"); // will throw SAXException: can't find matching start element
```

**NOTE:**

- You can mark as many times as possible. i.e, multiple marks can exist;
- `endElements()` will only end elements which are started after recent mark.
- `release()` implictly does end elements

you can also release any mark, instead of last mark as below:

```java
int mark = xml.mark();
...
xml.mark();
...
xml.mark();
...
xml.release(mark);
```

when you call `mark()`, it returns the number of mark;  
first call to `mark()` returns 1. next call to `mark()` will return 2, if earlier mark is not released;

**NOTE:** there is an implicit mark 0, which should not be released by user. it is used by `XMLDocument`

## XMLDocument Wrappers ##

You can create wrappers for `XMLDocument` to make creating specific type of xml document easier;

For example: JLibs has one such wrapper `jlibs.xml.xsd.XSDocument` to create XMLSchema documents easily;

```java
import jlibs.xml.xsd.XSDocument;

XSDocument xsd = new XSDocument(new StreamResult(System.out), false, 4, null);
xsd.startDocument();
{
    String n1 = "http://www.example.com/N1";
    String n2 = "http://www.example.com/N2";
    xsd.xml().declarePrefix("n1", n1);
    xsd.xml().declarePrefix("n2", n2);
    xsd.startSchema(n1);
    {
        xsd.addImport(n2, "imports/b.xsd");
        xsd.startComplexType().name("MyType");
        {
            xsd.startCompositor(Compositor.SEQUENCE);
            xsd.startElement().ref(n1, "e1").endElement();
            xsd.endCompositor();
        }
        xsd.endComplexType();
        xsd.startElement().name("root").type(n1, "MyType").endElement();
    }
    xsd.endSchema();
}
xsd.endDocument();
```

produces following output:

```xml
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema" targetNamespace="http://www.example.com/N1" xmlns:n1="http://www.example.com/N1" xmlns:n2="http://www.example.com/N2">
    <xsd:import namespace="http://www.example.com/N2" schemaLocation="imports/b.xsd"/>
    <xsd:complexType name="MyType">
        <xsd:sequence>
            <xsd:element ref="n1:e1"/>
        </xsd:sequence>
    </xsd:complexType>
    <xsd:element name="root" type="n1:MyType"/>
</xsd:schema>
```

You can also create similar wrappers.

## ObjectInputSource ##

`org.xml.sax.InputSource` wraps `systemID` or `OutputStream` or `Reader` which is source of xml;

Similarly, `ObjectInputSource` extends `InputSource` and wraps a java object, which is the source of xml:

```java
new ObjectInputSource<E>(E obj, XMLDocument xml)
```

`ObjectInputSource` has with single abstract method:

```java
protected abstract void write(E obj, XMLDocument xml) throws SAXException;
```

Subclasses override this method and fire SAX events.

Let us write an implementation of `ObjectInputSource` for `Company`:

```java
import jlibs.xml.sax.ObjectInputSource;
import org.xml.sax.SAXException;

public class CompanyInputSource extends ObjectInputSource<Company>{
    public CompanyInputSource(Company company){
        super(company);
    }

    @Override
    protected void write(Company company, XMLDocument xml) throws SAXException{
        xml.startElement("company");
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
        xml.endElement("company");
    }
}
```

**Note:** `xml.startDocument()` is implicitly called before `write(...)` method and
`xml.endDocument()` is called implicitly after `write(...)` method.

To create XML, now we can do the following:

```java
public static void main(String[] args) throws Exception{
    Employee scott = new Employee("1", "scott", "scott@gmail.com", 20);
    Employee alice = new Employee("2", "alice", "alice@gmail.com", 25);
    Company company = new Company("MyCompany", scott, alice);

    // print company to System.out as xml
    new CompanyInputSource(company).writeTo(System.out, false, 4, null);
}
```

`ObjectInputSource` contains several methods to serialize the SAX events:

```java
public void writeTo(Writer writer, boolean omitXMLDeclaration, int indentAmount) throws TransformerException
public void writeTo(OutputStream out, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerException
public void writeTo(String systemID, boolean omitXMLDeclaration, int indentAmount, String encoding) throws TransformerException
```

if `encoding` is null, then it defaults to default XML encoding(`UTF-8`);  
These `writeTo(...)` methods use Identity Trasformer;



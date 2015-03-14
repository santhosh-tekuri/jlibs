the package `jlibs.xml.sax.binding` contains classes which help you to read xml and create java objects.

### Concepts ###
Before going into details, we will first go through the concepts. Then it will be easier to understand the code.

we have employee.xml:
```
<employee>                     
    <name>scott</name>         
    <age>20</age>              
    <experience>5</experience> 
</employee>
```
and Employee class:
```
public class Employee{
    private String name;
    private int age;
    private int experience;

    // getter and setter methods
}
```

Each element in xml will create a java object.
Let us see the java objects created for above xml:
```
        XML                     |     Java Object  
--------------------------------|-----------------
<employee>                      |   new Employee()
    <name>scott</name>          |   new String(#text) 
    <age>20</age>               |   new String(#text) 
    <experience>5</experience>  |   new String(#text) 
</employee>                     |
```
From above table you can see that:
  * `<employe/>` element create new `Employee` object<br>
<ul><li><code>&lt;name/&gt;</code>, <code>&lt;age/&gt;</code> and <code>&lt;experience/&gt;</code> elements create <code>String</code> objects with their text content(i.e <code>#text</code>)</li></ul>

Now we have 4 java Objects ( one <code>Employee</code> object and four <code>String</code> objects)<br>
<br>
Now <b>relation</b> comes into picture. Each element has a relation which tells how to relate current element's java object with parent element's java oject.<br>
<br>
<pre><code>        XML                     |     Java Object             |   Relation<br>
--------------------------------|--------------------------------------------------------<br>
&lt;employee&gt;                      |   emp = new Employee()      |   - No Relation -<br>
    &lt;name&gt;scott&lt;/name&gt;          |   name = new String(#text)  | emp.setName(name)<br>
    &lt;age&gt;20&lt;/age&gt;               |   age = new String(#text)   | emp.setAge(age)<br>
    &lt;experience&gt;5&lt;/experience&gt;  |   exp = new String(#text)   | emp.setExperience(exp)<br>
&lt;/employee&gt;                     |<br>
</code></pre>

The above table shows how java objects created are related with each other.<br>
To make understanding easier, we assigned each java object created into some variable.<br>
<ul><li><code>Employee</code> object created is assigned to <code>emp</code> variable<br>
</li><li><code>String</code> created for <code>&lt;name&gt;</code> element is assigned to <code>name</code> variable<br>
</li><li><code>String</code> created for <code>&lt;age&gt;</code> element is assigned to <code>age</code> variable<br>
</li><li><code>String</code> created for <code>&lt;experience&gt;</code> element is assigned to <code>exp</code> variable</li></ul>

Now you can see that relation of <code>&lt;name&gt;</code> element and its parent element <code>&lt;employee&gt;</code> in java is:<br>
<pre><code>emp.setName(name)<br>
</code></pre>
Once Java-Object and Relation are defined for each element type, It is piece of cake to read xml document into java objects.<br>
<hr />

<h3>Implementing Binding</h3>
<pre><code>import jlibs.xml.sax.binding.*;<br>
<br>
@Binding("employee")<br>
public class EmployeeBinding{<br>
    @Binding.Start<br>
    public static Employee onStart() throws SAXException{<br>
        return new Employee();<br>
    }<br>
<br>
    @Binding.Text({"name", "age", "experience"})<br>
    public static String onText(String text){<br>
        return text;<br>
    }<br>
<br>
    @Relation.Finish("name")<br>
    public static void relateName(Employee employee, String name){<br>
        employee.setName(name);<br>
    }<br>
<br>
    @Relation.Finish("age")<br>
    public static void relateAge(Employee employee, String age){<br>
        employee.setAge(Integer.parseInt(age));<br>
    }<br>
<br>
    @Relation.Finish("experience")<br>
    public static void relateExperience(Employee employee, String experience){<br>
        employee.setExperience(Integer.parseInt(experience));<br>
    }<br>
}<br>
</code></pre>

Let us walk through the code:<br>
<br>
<pre><code>import jlibs.xml.sax.binding.*;<br>
</code></pre>
package <code>jlibs.xml.sax.binding</code> contains various annotations, which we use to define binding.<br>
<br>
<pre><code>@Binding("employee")<br>
public class EmployeeBinding{<br>
</code></pre>
<code>@Binding("employee")</code> annotation says that, <code>EmployeeBinding</code> class defines binding for <code>&lt;employee&gt;</code> element<br>
<br>
<pre><code>@Binding.Start<br>
public static Employee onStart() throws SAXException{<br>
    return new Employee();<br>
}<br>
</code></pre>
<code>@Binding.Start</code> annotation says that, when <code>&lt;employee&gt;</code> element starts call this method.<br>
this method returns new <code>Employee</code> object. i.e for each <code>&lt;employee&gt;</code> we create an <code>Employee</code> object.<br>
<br>
<pre><code>@Binding.Text({"name", "age", "experience"})<br>
public static String onText(String text){<br>
    return text;<br>
}<br>
</code></pre>
<code>@Binding.Text({"name", "age", "experience"})</code> annotation says that, call this method for <code>&lt;name&gt;</code>, <code>&lt;age&gt;</code> and <code>&lt;employee&gt;</code> text content.<br>
The argument <code>text</code> will be the text content of that element. The java object created for these elements is their text<br>
content, so we simply return the <code>text</code> argument.<br>
<br>
<pre><code>@Relation.Finish("name")<br>
public static void relateName(Employee employee, String name){<br>
    employee.setName(name);<br>
}<br>
</code></pre>
<code>@Relation.Finish("name")</code> annotation says that, call this method on <code>&lt;name&gt;</code> element end.<br>
The first argument will be the java object created for <code>&lt;name&gt;</code>'s parent element (i,e <code>&lt;employee&gt;</code> element), which is <code>Employee</code> object created by <code>onStart()</code> method.<br>
The second argument will be the java object created for <code>&lt;name&gt;</code> element, which is <code>String</code> object created by <code>onText(...)</code> method.<br>
<br>
similarly <code>relateAge(...)</code> and <code>relateExperience(...)</code> are called on <code>&lt;age&gt;</code> and <code>&lt;experience&gt;</code> element end respectively.<br>
<hr />

<h3>SAX Parsing</h3>
Now we have finished coding <code>EmployeeBinding</code>. Now let us see how to read employee xml document using this binding.<br>
<pre><code>public static Employee read(File xmlFile) throws Exception{<br>
    BindingHandler handler = new BindingHandler(EmployeeBinding.class);<br>
    return (Employee)handler.parse(new InputSource(xmlFile.getPath()));<br>
}<br>
</code></pre>

<code>BindingHandler</code> is an implementation of SAX <code>DefaultHandler</code>. It's constructor takes the binding calss as argument.<br>
<hr />

<h3>Behind the Scene</h3>

Let us see what happens behind the scene.<br>
<br>
All the annoations we have used have <code>RetentionPolicy.SOURCE</code>(except <code>@Binding</code>). i.e These annotations are not available at runtime.<br>
<code>JLibs</code> comes with an <a href='http://code.google.com/p/jlibs/source/browse/trunk/xml/src/jlibs/xml/sax/binding/BindingAnnotationProcessor.java'>annotation processor</a>,<br>
which processes these annotations at compile time.<br>
This processor generates a class for each class with <code>@Binding</code> annotation.<br>
This generated class defines the <a href='http://en.wikipedia.org/wiki/State_diagram'>state diagram</a> for the binding.<br>
<br>
for example when you compile <code>EmployeeBinding</code> class, you will get an additional class <code>EmployeeBindingImpl</code> generated.<br>
<br>
<code>BindingHandler</code> is a SAX <code>DefaultHandler</code> which implements a <a href='http://en.wikipedia.org/wiki/Finite-state_machine'>state machine</a>.<br>
<br>
Because reflection is not used at runtime, the sax parsing will be faster.<br>
<hr />
<h3>Unexpected Elements</h3>

let us say employee.xml is:<br>
<pre><code>&lt;employee&gt;                     <br>
    &lt;name&gt;scott&lt;/name&gt;         <br>
    &lt;age&gt;20&lt;/age&gt;              <br>
    &lt;experience&gt;5&lt;/experience&gt; <br>
    &lt;email&gt;scott@email.com&lt;/email&gt; <br>
&lt;/employee&gt;<br>
</code></pre>
The above xml document has an unexpected element <code>&lt;email&gt;</code> for which we have not defined any binding.<br>
<br>
When you read the above xml document using <code>EmployeeBinding</code>, it simply ignores the undefinded element <code>&lt;email&gt;</code>.<br>
i.e you will be able to create <code>Employee</code> object from the above xml document without any errors.<br>
<br>
Suppose you want to issue an error for undefined elements then do:<br>
<pre><code>handler.setIgnoreUnresolved(false); // default is true<br>
</code></pre>

now when you try to read the above xml document, you will get following exception:<br>
<pre><code>org.xml.sax.SAXException: can't find binding for /employee/email (line=5, col=12)<br>
</code></pre>
<hr />
<h3>Reusing Bindings</h3>

Have a look at <code>Company</code> class:<br>
<pre><code>public class Company{<br>
    private String name;<br>
    private Employee manager;<br>
    public List&lt;Employee&gt; employees = new ArrayList&lt;Employee&gt;();<br>
    <br>
    // getter and setter methods<br>
}<br>
</code></pre>

and company.xml as below:<br>
<pre><code>        XML                        |     Java Object                   |   Relation<br>
-----------------------------------|-------------------------------------------------------------------<br>
&lt;company name="foo"&gt;               | company = new Company(@name)      |<br>
    &lt;manager&gt;                      | manager = /*use EmployeeBinding*/ |<br>
        &lt;name&gt;admin&lt;/name&gt;         |                                   |<br>
        &lt;age&gt;30&lt;/age&gt;              |                                   |<br>
        &lt;experience&gt;7&lt;/experience&gt; |                                   |<br>
    &lt;/manager&gt;                     |                                   | company.setManager(manager)<br>
    &lt;employee&gt;                     | employee = /*use EmployeeBinding*/|<br>
        &lt;name&gt;scott&lt;/name&gt;         |                                   |<br>
        &lt;age&gt;20&lt;/age&gt;              |                                   |<br>
        &lt;experience&gt;5&lt;/experience&gt; |                                   |<br>
    &lt;/employee&gt;                    |                                   | company.addEmployee(employee)<br>
    &lt;employee&gt;                     | employee = /*use EmployeeBinding*/|<br>
        &lt;name&gt;alice&lt;/name&gt;         |                                   |<br>
        &lt;age&gt;21&lt;/age&gt;              |                                   |<br>
        &lt;experience&gt;4&lt;/experience&gt; |                                   |<br>
    &lt;/employee&gt;                    |                                   | company.addEmployee(employee)<br>
&lt;/company&gt;                         |                                   |<br>
</code></pre>

on <code>&lt;company&gt;</code> element begin, we create <code>Company</code> object.<br>
for <code>&lt;manager&gt;</code> and <code>&lt;employee&gt;</code> elements we will create <code>Employee</code> objects, using <code>EmployeeBinding</code> coded earlier.<br>
on <code>&lt;manager&gt;</code> element end, we relate <code>company</code> and <code>manager</code> objects using <code>setManager(...)</code> method<br>
on <code>&lt;employee&gt;</code> element end, we relate <code>company</code> and <code>employee</code> objects using <code>addEmployee(...)</code> method<br>
<br>
Let us implmennt <code>CompanyBinding</code>:<br>
<pre><code>@Binding("company")<br>
public class CompanyBinding{<br>
    @Binding.Start<br>
    public static Company onStart(@Attr String name) throws SAXException{<br>
        return new Company(name);<br>
    }<br>
<br>
    @Binding.Element(element = "manager", clazz = EmployeeBinding.class)<br>
    public static void onManager(){}<br>
<br>
    @Relation.Finish("manager")<br>
    public static void relateManager(Company company, Employee manager){<br>
        company.setManager(manager);<br>
    }<br>
<br>
    @Binding.Element(element = "employee", clazz = EmployeeBinding.class)<br>
    public static void onEmployee(){}<br>
<br>
    @Relation.Finish("employee")<br>
    public static void relateEmployee(Company company, Employee employee){<br>
        company.employees.add(employee);<br>
    }<br>
}<br>
</code></pre>
Let us walk through the code:<br>
<pre><code>@Binding.Start<br>
public static Company onStart(@Attr String name) throws SAXException{<br>
    return new Company(name);<br>
}<br>
</code></pre>
<code>@Attr</code> annotation is used to get attribute value.<br>
the attribute name is derived from the parameter name.<br>
i.e, <code>@Attr String name</code> will give value of attribute <code>name</code><br>

in some cases, attribute name may not be valid java identifier.<br>
for example <code>param-name</code> is a valid attribute name, but not a valid java identifier. <br>
in such cases you can do:<br>
<pre><code>@Attr("param-name") String paramName<br>
</code></pre>

<pre><code>@Binding.Element(element = "manager", clazz = EmployeeBinding.class)<br>
public static void onManager(){}<br>
</code></pre>
<code>@Binding.Element(element = "manager", clazz = EmployeeBinding.class)</code> annotation says to reuse <code>EmployeeBinding</code> for <code>&lt;manager&gt;</code> element.<br>
<br>
<hr />
<h3>Namespace Support</h3>
<pre><code>@NamespaceContext({<br>
    @Entry(prefix="foo", uri="http://www.foo.com"),<br>
    @Entry(prefix="bar", uri="http://www.bar.com")<br>
})<br>
@Binding("foo:employee")<br>
public class EmployeeBinding{<br>
    ...<br>
}<br>
</code></pre>

<code>@NamespaceContext</code> annotation is used to define prefix to namespace mappings.<br>
then you can use these prefixes in remaining annotations. for example:<br>
<pre><code>@Binding("foo:employee")<br>
</code></pre>

<hr />
<h3>When no-arg constructor is missing</h3>

Let us say our <code>Employee</code> has no default constructor:<br>
<pre><code>public class Employee{<br>
    private String name;<br>
    private int age;<br>
    private int experience;<br>
<br>
    public Employee(String name, int age, int experience){<br>
        this.name = name;<br>
        this.age = age;<br>
        this.experience = experience;<br>
    }<br>
<br>
    // getter methods<br>
}<br>
</code></pre>
Let us see how to handle this situation:<br>
<pre><code>        XML                     |     Java Object                                                     |   Relation<br>
--------------------------------|---------------------------------------------------------------------|----------------------------<br>
&lt;employee&gt;                      |                                                                     |<br>
    &lt;name&gt;scott&lt;/name&gt;          | name = new String(#text)                                            | parent[&lt;name&gt;] = name<br>
    &lt;age&gt;20&lt;/age&gt;               | age = new String(#text)                                             | parent[&lt;age&gt;] = age<br>
    &lt;experience&gt;5&lt;/experience&gt;  | exp = new String(#text)                                             | parent[&lt;experience&gt;] = exp<br>
&lt;/employee&gt;                     | emp = new Employee(current[&lt;name&gt;], current[&lt;age&gt;], [&lt;experience&gt;]) | <br>
</code></pre>

Notice that now we are creating <code>Employee</code> object in <code>&lt;employee&gt;</code> element end, rather than <code>&lt;employee&gt;</code> element begin.<br>
This is because, the values of name, age and experience are available only on <code>&lt;employee&gt;</code> element end.<br>

Earlier, the relation for <code>&lt;name&gt;</code>, <code>&lt;age&gt;</code> and <code>&lt;experience&gt;</code> used to call respective set method on <code>Employee</code> object.<br>
Now we can't do that, because we don't have <code>Employee</code> created by that time.<br>
<br>
For each element in xml document, a <a href='http://code.google.com/p/jlibs/source/browse/trunk/xml/src/jlibs/xml/sax/binding/SAXContext.java'>SAXContext</a> is maintained by <code>BindingHandler</code>.<br>
This context is used to store the java object you created for that element.<br>
<code>SAXContext.object</code> gives the object you have created.<br>
<br>
Other than that, <code>SAXContext</code> also has map, which you can use to store values temporarly which are required to create the java object later.<br>
<code>SAXContext.temp</code> is that map. here key is <code>QName</code> and value is any <code>Object</code>.<br>
<br>
let us see the relation for <code>&lt;name&gt;</code> element:<br>
<pre><code>parent[&lt;name&gt;] = name<br>
</code></pre>
here <code>parent</code> refers to parent element's (i,e <code>&lt;employee&gt;</code>) context;<br>
we are storing the <code>String</code> object created for <code>&lt;name&gt;</code> element, in <code>&lt;employee&gt;</code>'s context.<br>
here the key used is the qname name of current element i.e <code>&lt;name&gt;</code>

similarly we are saving the values of <code>&lt;age&gt;</code> and <code>&lt;experience&gt;</code> elements also in <code>&lt;employees&gt;</code>'s context.<br>
<br>
now on <code>&lt;employee&gt;</code> element end we do:<br>
<pre><code>emp = new Employee(current[&lt;name&gt;], current[&lt;age&gt;], [&lt;experience&gt;])<br>
</code></pre>
here <code>current</code> refers to current element's (i.e <code>&lt;employee&gt;</code>) context;<br>
we are retriving the values of <code>&lt;name&gt;</code>, <code>&lt;age&gt;</code> and <code>&lt;experience&gt;</code> stored earlier, and creating <code>Employee</code> object using them.<br>
<br>
Let us see what it looks like in java code:<br>
<pre><code>@Binding("employee")<br>
public class EmployeeBinding{<br>
    @Binding.Text({"name", "age", "experience"})<br>
    public static String onText(String text){<br>
        return text;<br>
    }<br>
<br>
    @Relation.Finish({"name", "age", "experience"})<br>
    public static String relateWithEmployee(String value){<br>
        return value;<br>
    }<br>
<br>
    @Binding.Finish<br>
    public static Employee onFinish(@Temp String name, @Temp String age, @Temp String experience) throws SAXException{<br>
        return new Employee(name,<br>
                Integer.parseInt(age==null ? "0" : age),<br>
                Integer.parseInt(experience==null ? "0" : experience)<br>
        );<br>
    }<br>
}<br>
</code></pre>
Let us walk through the code;<br>
<pre><code>@Relation.Finish({"name", "age", "experience"})<br>
public static String relateWithEmployee(String value){<br>
    return value;<br>
}<br>
</code></pre>
<code>@Relation.Finish({"name", "age", "experience"})</code> annotation says that, call this method on<br>
<code>&lt;name&gt;</code>, <code>&lt;age&gt;</code> and <code>&lt;experience&gt;</code> element end.<br>
<br>
When a method with relation annotation returns something:<br>
<ul><li>the returned value is stored in parent element's temp with current element's qname as key<br>
</li><li>the first argument will be the object created for the current element.</li></ul>

i.e <code>String value</code> is the <code>String</code> object created for current element.<br>
and the value returned is stored in <code>&lt;employee&gt;</code> element's temp.<br>
<br>
<pre><code>@Binding.Finish<br>
public static Employee onFinish(@Temp String name, @Temp String age, @Temp String experience) throws SAXException{<br>
    return new Employee(name,<br>
            Integer.parseInt(age==null ? "0" : age),<br>
            Integer.parseInt(experience==null ? "0" : experience)<br>
    );<br>
}<br>
</code></pre>
<code>@Binding.Finish</code> annotation says that, call this method on <code>&lt;employee&gt;</code> element end.<br>

<code>@Temp String name</code> says that give me the value mapped to <code>name</code> in current element's temp.<br>
similar to <code>@Attr</code> you can explicitly specify the qname as follows:<br>
<pre><code>@Temp("name") String employeeName<br>
</code></pre>

Here in <code>onFinish(...) method, we are creating </code>Employee` object using the values that are stored earlier in temp, and returning it.<br>
<hr />
<h3>Recursive Bindings</h3>
Let us say we have <code>Component</code> class:<br>
<pre><code>public class Component{<br>
    public final String name;<br>
    public Properties initParams = new Properties();<br>
    public List&lt;Component&gt; dependencies = new ArrayList&lt;Component&gt;();<br>
<br>
    public Component(String name){<br>
        this.name = name;<br>
    }<br>
}<br>
</code></pre>
each component has:<br>
<ul><li>name<br>
</li><li>init params<br>
</li><li>dependencies (list of components) i.e, we have recursion</li></ul>

Let us see sample xml:<br>
<pre><code>        XML                                               |     Java Object                                                     |   Relation<br>
----------------------------------------------------------|---------------------------------------------------------------------|----------------------------<br>
&lt;component name="comp1"&gt;                                  | comp = new Company(@name)                                           |<br>
    &lt;init-param&gt;                                          |                                                                     |<br>
        &lt;param-name&gt;param1&lt;/param-name&gt;                   | paramName = #text                                                   | parent[&lt;param-name&gt;] = paramName<br>
        &lt;param-value&gt;value1&lt;/param-value&gt;                 | paramValue = #text                                                  | parent[&lt;param-value&gt;] = paramValue<br>
    &lt;/init-param&gt;                                         | comp.initParams.put(current[&lt;param-name&gt;], current[&lt;param-value&gt;])  |<br>
    &lt;init-param&gt;                                          |                                                                     |<br>
        &lt;param-name&gt;param2&lt;/param-name&gt;                   |                                                                     |<br>
        &lt;param-value&gt;value2&lt;/param-value&gt;                 |                                                                     |<br>
    &lt;/init-param&gt;                                         |                                                                     |<br>
    &lt;dependencies&gt;                                        |                                                                     |<br>
        &lt;component name="comp2"&gt;                          | dependent = /* use Recursion */                                     |<br>
            &lt;init-param&gt;                                  |                                                                     |<br>
                &lt;param-name&gt;param3&lt;/param-name&gt;           |                                                                     |<br>
                &lt;param-value&gt;value3&lt;/param-value&gt;         |                                                                     |<br>
            &lt;/init-param&gt;                                 |                                                                     |<br>
            &lt;dependencies&gt;                                |                                                                     |<br>
                &lt;component name="comp3"&gt;                  |                                                                     |<br>
                    &lt;init-param&gt;                          |                                                                     |<br>
                        &lt;param-name&gt;param4&lt;/param-name&gt;   |                                                                     |<br>
                        &lt;param-value&gt;value4&lt;/param-value&gt; |                                                                     |<br>
                    &lt;/init-param&gt;                         |                                                                     |<br>
                &lt;/component&gt;                              |                                                                     |<br>
            &lt;/dependencies&gt;                               |                                                                     |<br>
        &lt;/component&gt;                                      |                                                                     | comp.dependencies.add(dependent)<br>
    &lt;/dependencies&gt;                                       |                                                                     |<br>
&lt;/component&gt;                                              |                                                                     |<br>
</code></pre>
here we have recursion. <code>comp1</code> depends on <code>comp2</code> which in turn depends on <code>comp3</code>

let us the Binding implementation:<br>
<pre><code>@Binding("component")<br>
public class ComponentBinding{<br>
    @Binding.Start<br>
    public static Component onStart(@Attr String name){<br>
        return new Component(name);<br>
    }<br>
<br>
    @Binding.Text({ "init-param/param-name", "init-param/param-value" })<br>
    public static String onParamNameAndValue(String text){<br>
        return text;<br>
    }<br>
<br>
    @Relation.Finish({"init-param/param-name", "init-param/param-value"})<br>
    public static String relateParamNameAndValue(String content){<br>
        return content;<br>
    }<br>
<br>
    @Relation.Finish("init-param")<br>
    public static void relateParam(Component comp, @Temp("param-name") String paramName, @Temp("param-value") String paramValue){<br>
        comp.initParams.put(paramName, paramValue);<br>
    }<br>
<br>
    @Binding.Element(element="dependencies/component", clazz=ComponentBinding.class)<br>
    public static void onDependecy(){}<br>
<br>
    @Relation.Finish("dependencies/component")<br>
    public static void relateDependency(Component comp, Component dependent){<br>
        comp.dependencies.add(dependent);<br>
    }<br>
}<br>
</code></pre>
<hr />
<h3>Storing List of values in temp</h3>
<pre><code>public class Employee{<br>
    public String name;<br>
    public int age;<br>
    public int experience;<br>
    public String contacts[];<br>
<br>
    public Employee(String name, int age, int experience){<br>
        this.name = name;<br>
        this.age = age;<br>
        this.experience = experience;<br>
    }<br>
}<br>
</code></pre>
<pre><code>        XML                                     |     Java Object                              |   Relation<br>
------------------------------------------------|----------------------------------------------|----------------------------<br>
&lt;employee name="scott" age="20" experience="5"&gt; | emp = new Employee(@name, @age, @experience) |<br>
    &lt;contacts&gt;                                  |                                              |<br>
        &lt;email&gt;scott@yahoo.com&lt;/email&gt;          | email = #text                                | parent[&lt;email&gt;] += email<br>
        &lt;email&gt;scott@google.com&lt;/email&gt;         | email = #text                                | parent[&lt;email&gt;] += email<br>
        &lt;email&gt;scott@msn.com&lt;/email&gt;            | email = #text                                | parent[&lt;email&gt;] += email<br>
    &lt;/contacts&gt;                                 |                                              | emp.contacts = current[&lt;email&gt;]<br>
&lt;/employee&gt;                                     |                                              |<br>
</code></pre>

notice that the relation for <code>&lt;email&gt;</code> element end is:<br>
<pre><code>parent[&lt;email&gt;] += email<br>
</code></pre>
here '+=' means add to temp (i.e don't replace existing value)<br>
that is, <code>parent[&lt;email&gt;]</code> value is a list of strings rather than string<br>
<br>
the relation for <code>&lt;contacts&gt;</code> element end is:<br>
<pre><code>emp.contacts = parent[&lt;email&gt;]<br>
</code></pre>
i.e we are assigning the list of emails from current element's temp int <code>emp.conctacts</code>

Let us see the java code:<br>
<pre><code>@Binding("employee")<br>
public class EmployeeBinding{<br>
    @Binding.Start<br>
    public static Employee onStart(@Attr String name, @Attr String age, @Attr String experience){<br>
        return new Employee(name,<br>
                age!=null ? Integer.parseInt(age) : 0,<br>
                experience!=null ? Integer.parseInt(experience) : 0<br>
        );<br>
    }<br>
<br>
    @Binding.Text("contacts/email")<br>
    public static String onText(String text){<br>
        return text;<br>
    }<br>
<br>
    @Relation.Finish("contacts/email")<br>
    public static @Temp.Add String relateEmail(String email){<br>
        return email;<br>
    }<br>
<br>
    @Binding.Finish("contacts")<br>
    public static void onFinish(Employee emp, @Temp("email") List&lt;String&gt; emails){<br>
        emp.contacts = emails.toArray(new String[emails.size()]);<br>
    }<br>
}<br>
</code></pre>
let us walk through the code:<br>
<pre><code>@Relation.Finish("contacts/email")<br>
public static @Temp.Add String relateEmail(String email){<br>
    return email;<br>
}<br>
</code></pre>
<code>@Temp.Add</code> on return type says that, add the returned value to the existing value.<br>
i.e we want to save it as list of emails<br>
<br>
<pre><code>@Binding.Finish("contacts")<br>
public static void onFinish(Employee emp, @Temp("email") List&lt;String&gt; emails){<br>
    if(emails!=null)<br>
        emp.contacts = emails.toArray(new String[emails.size()]);<br>
}<br>
</code></pre>
notice the second argument. It is mapped to <code>@Temp("email")</code> and its type is <code>List&lt;String&gt;</code>
<hr />
<h3>Comparision with existing Binding Frameworks</h3>
Most of java-xml binding implementations provide two way support.<br>
<ul><li>serializing domain object to xml<br>
</li><li>serializing xml to domain object.</li></ul>

But JLibs implementation is only one way. that is "deserializing xml to domain object".<br>
to serialize xml to domain object, you can use <code>XMLDocument</code>

the main advantages of JLibs implementation:<br>
<ul><li>works with hand-coded domain objects</li></ul>

<blockquote>most binding implementations mandate that domain objects has to be generated<br>
from schema or dtd. They don't work with hand-coded domain objects</blockquote>

<ul><li>domain objects are not tied to binding implementation</li></ul>

<blockquote>domain objects dont need to extend/implement a particular class/interface from binding implementation.<br>
for example,<br>
<ul><li>XMLBeans mandates that all domain objects implement <code>org.apache.xmlbeans.XmlObject</code><br>
</li><li>Similarly EMF mandates that all domain objects implement <code>EObject</code> interface<br></li></ul></blockquote>

<ul><li>domain objects are light-weight. i.e not poluted with binding implementation specific information</li></ul>

<blockquote>for example: the domain objects created by XMLBeans or EMF carry lot of information which are specific<br>
to them. This will bloat up memory.</blockquote>

<ul><li>easier migration.</li></ul>

<blockquote>let us say in version 2 of your project, you want to change the structure of xml how it looks like, and still wants to provide backward<br>
compatibility to end-users.</blockquote>

<blockquote>This is tedious task with other binding frameworks. With JLibs you can have different Binding implementations<br>
for a domain object and use appropriate one at runtime based on version of xml document.</blockquote>

<ul><li>clear separation of binding and domain object<br>
</li><li>in jlibs, it is like a callback methods.</li></ul>

<blockquote>callback methods can define when it has to be called and what information from xml document you want. <br>
You have complete control how to consume that information into domain object (because you are implementing it in java code)</blockquote>

<ul><li>The runtime memory used by jlibs binding is minimal and no reflection api is used. The number of <code>SAXContext</code> is equal to the maximum element depth of the xml document.</li></ul>

JLibs binding implementation more of resembels <a href='http://commons.apache.org/digester/'>Apache's Commons-Digestor</a> but without reflection<br>
<br>
<br>
<h3>Your Feedback is valuable</h3>
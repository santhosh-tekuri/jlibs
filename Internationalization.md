In traditional approach, we create properties file;<br>
But with JLibs you create an interface and annotate it with <code>@ResourceBundle</code><br>
Let us see sample code.<br>
<br>
<pre><code>import jlibs.core.util.i18n.I18N;<br>
import jlibs.core.util.i18n.Message;<br>
import jlibs.core.util.i18n.ResourceBundle;<br>
<br>
@ResourceBundle<br>
public interface DBBundle{<br>
    public static final DBBundle DB_BUNDLE = I18N.getImplementation(DBBundle.class);<br>
    <br>
    @Message("SQL Execution completed in {0} seconds with {1} errors")<br>
    public String executionFinished(long seconds, int errorCount);<br>
<br>
    @Message(key="SQLExecutionException", value="Encountered an exception while executing the following statement:\n{0}")<br>
    public String executionException(String query);<br>
<br>
    @Message("executing {0}")<br>
    public String executing(String query);<br>
}<br>
</code></pre>
let us walk through code.<br>
<br>
<pre><code>@ResourceBundle<br>
public interface DBBundle{<br>
</code></pre>
<code>@ResourceBundle</code> says that this interface is used for I18N purpose.<br>
this annotation can be applied only on interface.<br>
<br>
all methods in this interface should be annotated with <code>@Message</code>.<br>
For each message you want, you will add a method in this interface<br>
<pre><code>@Message("SQL Execution completed in {0} seconds with {1} errors")<br>
public String executionFinished(long seconds, int errorCount);<br>
</code></pre>
here the key of message is the name of the method. i.e, <code>executionFinished</code><br>
and the value of message is <code>SQL Execution completed in {0} seconds with {1} errors</code>

<pre><code>@Message(key="SQLExecutionException", value="Encountered an exception while executing the following statement:\n{0}")<br>
public String executionException(String query);<br>
</code></pre>
here we are explicitly specifying key as <code>SQLExecutionException</code>

When you compile this interface with <code>jlibs-core.jar</code> in classpath, it will generate:<br>
<ul><li><code>Bundle.properties</code> which contains the messages<br>
</li><li><code>_Bundle.class</code> which implements your interface and each method implementation returns its corresponding message from <code>Bundle.properties</code></li></ul>

<pre><code>public static final DBBundle DB_BUNDLE = I18N.getImplementation(DBBundle.class);<br>
</code></pre>
<code>I18N.getImplementation(DBBundle.class)</code> returns an instance of <code>_Bundle</code> class that is generated.<br>
<br>
You can have more than one interface with <code>@ResourceBundle</code> in a package. In such case:<br>
<ul><li>generated <code>Bundle.properties</code> will have messages from all interfaces<br>
</li><li>generated <code>_Bundle.class</code> will be implementing all these interfaces</li></ul>

i.e it would be easier to group messages based on the context they are used.<br>
Let us say I have UIBundle interface in same package, which contains messages used by UI:<br>
<br>
<pre><code>@ResourceBundle<br>
public interface UIBundle{<br>
    public static final UIBundle UI_BUNDLE = I18N.getImplementation(UIBundle.class);<br>
<br>
    @Message("Execute")<br>
    public String executeButton();<br>
    <br>
    @Message("File {0} already exists.  Do you really want to replace it?")<br>
    public String confirmReplace(File file);<br>
}<br>
</code></pre>

<code>DBBundle</code> contains all messages used in database interaction<br>
<code>UIBundle</code> contains all messages used by UI classes<br>
<br>
let us see sample code using these bundles:<br>
<pre><code>import static i18n.DBBundle.DB_BUNDLE;<br>
import static i18n.UIBundle.UI_BUNDLE;<br>
<br>
executeButton.setText(UI_BUNDLE.executeButton());<br>
<br>
try{<br>
    System.out.println(DB_BUNDLE.executing(query));<br>
    // execute query<br>
    System.out.println(DB_BUNDLE.executionFinished(5, 0));<br>
}catch(SQLException ex){<br>
    System.out.println(DB_BUNDLE.executionException(query));<br>
}<br>
</code></pre>

You can see that, the code looks clean without any hardcoded message keys.<br>
<br>
<hr />
<h3>Documentation</h3>
<pre><code>@Message("SQL Execution completed in {0} seconds with {1} errors")<br>
public String executionFinished(long seconds, int errorCount);<br>
</code></pre>
the message generated in <code>Bundle.properties</code> will be:<br>
<pre><code># {0} seconds<br>
# {1} errorCount<br>
executionFinished=SQL Execution completed in {0} seconds with {1} errors<br>
</code></pre>
the generated message tells what <code>{0}</code> and <code>{1}</code> are referring to.<br>
This makes the job of translator (who is translating to some other language) easier, because he/she now understand the message better.<br>
<br>
<pre><code>/**<br>
    * thrown when failed to load application<br>
    * because of network failure<br>
    *<br>
    * @param application   UID of application<br>
    * @param version       version of the application<br>
    */<br>
@Message(key = "cannotKillApplication", value="failed to kill application {0} with version {1}")<br>
public String cannotKillApplication(String application, String version);<br>
</code></pre>
the message generated in <code>Bundle.properties</code> will be:<br>
<pre><code># thrown when failed to load application<br>
# because of network failure<br>
# {0} application ==&gt; UID of application<br>
# {1} version ==&gt; version of the application<br>
cannotKillApplication=failed to kill application {0} with version {1}<br>
</code></pre>
i.e, any additional javadoc specified is also made available in generated <code>Bundle.properties</code>.<br>
This makes the job of translator more comfortable.<br>
<br>
<code>Bundle.properties</code> generated for DBBundle, UIBundle will look as below:<br>
<pre><code># DON'T EDIT THIS FILE. THIS IS GENERATED BY JLIBS<br>
# @author Santhosh Kumar T<br>
<br>
#-------------------------------------------------[ DBBundle ]---------------------------------------------------<br>
<br>
# {0} query<br>
executing=executing {0}<br>
<br>
# {0} query<br>
SQLExecutionException=Encountered an exception while executing the following statement\:\n{0}<br>
<br>
# {0} seconds<br>
# {1} errorCount<br>
executionFinished=SQL Execution completed in {0} seconds with {1} errors<br>
<br>
#-------------------------------------------------[ UIBundle ]---------------------------------------------------<br>
<br>
executeButton=Execute<br>
<br>
# {0} file<br>
confirmReplace=File {0} already exists.  Do you really want to replace it?<br>
</code></pre>

You can see that the messages from each interface are clearly separated in genrated properties file<br>
<br>
<hr />

<h3>Developer/IDE Friendly</h3>
<pre><code>import static i18n.DBBundle.DB_BUNDLE;<br>
import static i18n.UIBundle.UI_BUNDLE;<br>
<br>
executeButton.setText(UI_BUNDLE.executeButton());<br>
<br>
try{<br>
    System.out.println(DB_BUNDLE.executing(query));<br>
    // execute query<br>
    System.out.println(DB_BUNDLE.executionFinished(5, 0));<br>
}catch(SQLException ex){<br>
    System.out.println(DB_BUNDLE.executionException(query));<br>
}<br>
</code></pre>

the code using I18N messages is no longer cluttered with hardcoded strings. you never need to fear of<br>
<ul><li>misspelling message keys<br>
</li><li>specifying wrong number of arguments to message<br>
</li><li>specifying arguments in incorrect order to message</li></ul>

i.e you get complete compile-time safty, and IDE help, because messages are now java methods rather than hard-coded Strings<br>
<hr />
<h3>Invalid Messge Formats</h3>
<pre><code>@Message("your lass successfull login is on {0, timee}")<br>
public String lastSucussfullLogin(Date date);<br>
</code></pre>
here we misspelled the format <code>time</code> as <code>timee</code><br>
this will give following compile time error<br>
<pre><code>[javac] /jlibsuser/src/i18n/UIBundle.java:23: Invalid Message Format: unknown format type at <br>
[javac]     @Message("your lass successfull login is on {0, timee}")<br>
[javac]     ^<br>
</code></pre>
i.e any invalid message formats are caught at compile time<br>
<hr />
<h3>Argument Count Mismatch</h3>
<pre><code>@Message("SQL Execution completed in {0} seconds with {1} errors")<br>
public String executionFinished(long seconds);<br>
</code></pre>
here the message requires two arguments <code>{0}</code> and <code>{1}</code>. but the java method is taking only one argument.<br>
this will give following compile time error<br>
<pre><code>[javac] /jlibsuser/src/i18n/DBBundle.java:15: no of args in message format doesn't match with the number of parameters this method accepts<br>
[javac]     public String executionFinished(long seconds);<br>
[javac]                   ^<br>
</code></pre>
<hr />
<h3>Missing Argument</h3>
<pre><code>@Message("SQL Execution completed in {0} seconds with {2} errors and {2} warnings")<br>
public String executionFinished(long seconds, int errorCount, int warningCount);<br>
</code></pre>
here we misspelled <code>{1} errros</code> as <code>{2} errors</code>.<br>
this will give following compile time error<br>
<pre><code>[javac] /jlibsuser/src/i18n/DBBundle.java:14: {1} is missing in message<br>
[javac]     @Message("SQL Execution completed in {0} seconds with {2} errors and {2} warnings")<br>
[javac]     ^<br>
</code></pre>
<hr />
<h3>Duplicate Key</h3>
<pre><code>@Message(key="JLIBS015", value="SQL Execution completed in {0} seconds with {1} errors and {2} warnings")<br>
public String executionFinished(long seconds, int errorCount, int warningCount);<br>
<br>
@Message(key="JLIBS015", value="Encountered an exception while executing the following statement:\n{0}")<br>
public String executionException(String query);<br>
</code></pre>
here we accidently used same key <code>JLIBS015</code> for both methods.<br>
this will give following compile time error<br>
<pre><code>[javac] /jlibsuser/src/i18n/DBBundle.java:18: key 'JLIBS015' is already used by "java.lang.String executionFinished(long, int, int)" in i18n.DBBundle interface<br>
[javac]     public String executionException(String query);<br>
[javac]                   ^<br>
</code></pre>
<hr />
<h3>Method Signature Clash</h3>
<pre><code>public interface DBBundle{<br>
    ...<br>
    @Message(key="EXECUTING", value="executing {0}")<br>
    public String executing(String query);<br>
    ...<br>
}<br>
<br>
public interface UIBundle{<br>
    ...<br>
    @Message(key="EXECUTING_QUERY", value="executing {0}")<br>
    public String executing(String query);<br>
    ...<br>
}<br>
</code></pre>
here both <code>DBBundle</code> and <code>UIBundle</code> has methods with identical signature.<br>
The generated <code>_Bundle</code> class implements both the interfaces <code>DBBundle</code> and <code>UIBundle</code>,<br>
so it can't decide whether to use key <code>EXECUTING</code> or <code>EXECUTING_QUERY</code><br>
thus this will give following compile time error.<br>
<pre><code>[javac] /jlibsuser/src/i18n/UIBundle.java:27: clashes with similar method in i18n.DBBundle interface<br>
[javac]     public String executing(String query);<br>
[javac]                   ^<br>
</code></pre>
<hr />
<h3>Optimization</h3>
All annotations have source level retention policy. So there is no reflection used at runtime.<br>
<br>
Only one <code>_Bundle</code> class is generated per package, and this class will implement all interfaces with <code>@ResourceBundle</code> annatation in that package<br>
<br>
there is only one instance of <code>_Bundle</code> created by <code>I18N.getImplementation(clazz)</code><br>
i.e both <code>DBBundle.DB_BUNDLE</code> and <code>UIBundle.UI_BUNDLE</code> are referring to same instanceof <code>_Bundle</code>.<br>
<br>
the <code>_Bundle</code> class caches the <code>ResourceBundle</code> loaded.<br>
<hr />
<h3>Customization</h3>
You can change the name of the properties file generated by passing <code>-AResourceBundle.basename=MyBundle</code> to <code>javac</code><br>
this will create <code>MyBundle.properties</code>
<hr />
<h3>ErrorCodes</h3>
If you application uses error codes, as below:<br>
<br>
<pre><code>package com.foo.myapp;<br>
<br>
public class UncheckedException extends RuntimeException{<br>
    private String errorCode;<br>
<br>
    public UncheckedException(String errorCode, String message){<br>
        super(message);<br>
        this.errorCode = errorCode;<br>
    }<br>
    <br>
    public String getErrorCode(){<br>
        return errorCode;<br>
    }<br>
<br>
    @Override<br>
    public String toString(){<br>
        String s = getClass().getName()+": "+errorCode;<br>
        String message = getLocalizedMessage();<br>
        return (message != null) ? (s + ": " + message) : s;<br>
    }<br>
}<br>
</code></pre>

and in you application you always throw <code>UncheckedException</code> with different errorcode.<br>
<br>
In such case, you can internationalize errorcodes as below:<br>
<br>
<pre><code>package com.foo.myapp.controllers;<br>
<br>
public interface ErrorCodes{<br>
    public static final ErrorCodes INSTANCE = I18N.getImplementation(ErrorCodes.class);<br>
    <br>
    @Message("Database connection to host {0} is lost")<br>
    public UncheckedException connectionLost(String host);<br>
    <br>
    @Message("No book found titled {0} from author {1}")<br>
    public UncheckedException noSuchBookFound(String title, String author);<br>
}<br>
</code></pre>

now you can throw <code>UncheckedException</code> as follows:<br>
<pre><code>throw ErrorCodes.INSTANCE.connectionList(host);    <br>
throw ErrorCodes.INSTANCE.noSuchBookFound(title, author);<br>
</code></pre>

the exception class returned in <code>ErrorCodes</code> should have a constructor taking <code>errorCode</code> and <code>message</code> as arguments.<br>
the errorCode generated for <code>connectionLost</code> will be <code>myapp.controllers.ConnectionList</code>. i.e, the package and method name are<br>
joined. the top two packages are ignored and the first letter of method name is changed to uppercase.<br>
<br>
you can configure the number of top pakcages to be ignored by passing following option to annotation processor:<br>
<code>-AResourceBundle.ignorePackageCount=3</code>

The default value is <code>2</code>.<br>
If you want complete package name, then use value of <code>0</code><br>
If you do not want package name in errorcode , then use value of <code>-1</code><br>

<hr />
<h3>Internationalizing Domain Objects</h3>

Rather than internationalizing GUI, I would recomment internationalizing your domain objects.<br>
<br>
let us say your domain object is <code>Employee</code>:<br>
<pre><code>import jlibs.core.util.i18n.*;<br>
<br>
public class Employee{<br>
    public static final String PROP_NAME = "name";<br>
    public static final String PROP_AGE  = "age";<br>
    <br>
    @Bundle({<br>
        @Entry(hint=Hint.DISPLAY_NAME, rhs="User Name"),<br>
        @Entry(hint=Hint.DESCRIPTION, rhs="Full Name of Employee")<br>
    })<br>
    private String name;<br>
<br>
    @Bundle({<br>
        @Entry(hint=Hint.DISPLAY_NAME, rhs="Age"),<br>
        @Entry(hint=Hint.DESCRIPTION, rhs="Current Age of Employee")<br>
    })<br>
    private int age;<br>
    <br>
    // getter and setter methods<br>
}<br>
</code></pre>

here we are internationalizing each property of employee using <code>@Bundle</code> annotation.<br>
<pre><code>@Bundle({<br>
    @Entry(hint=Hint.DISPLAY_NAME, rhs="User Name"),<br>
    @Entry(hint=Hint.DESCRIPTION, rhs="Full Name of Employee")<br>
})<br>
private String name;<br>
</code></pre>
this will create following in <code>Bundle.properties</code>
<pre><code>Employee.name.displayName=User Name<br>
Employee.name.descritpion=Full Name of Employee<br>
</code></pre>
i.e each property generated is qualified by the class and field.<br>
<br>
now in Employee registration form, you can do:<br>
<pre><code>import jlibs.core.util.i18n.*;<br>
<br>
JLabel nameLabel = ...;<br>
JTextField nameField = ...;<br>
nameLabel.setText(Hint.DISPLAY_NAME.stringValue(Employee.class, Employee.PROP_NAME));<br>
nameField.setTooltipText(Hint.DESCRIPTION.stringValue(Employee.class, Employee.PROP_NAME));<br>
</code></pre>

let us say you have a JTable listing all employees, then you can do:<br>
<pre><code>import jlibs.core.util.i18n.*;<br>
<br>
Vector columnNames = new Vector();<br>
columnNames.add(Hint.DISPLAY_NAME.stringValue(Employee.class, Employee.PROP_NAME));<br>
columnNames.add(Hint.DISPLAY_NAME.stringValue(Employee.class, Employee.PROP_AGE));<br>
JTable table = new JTable(employees, columnNames);<br>
</code></pre>

Here we used same properties in two GUI Panes. We used same properties in both GUI<br>
to internationalize it.<br>
<br>
Moving internationalization from GUI to Domain Objects, allows:<br>
<ul><li>reusable properties<br>
</li><li>a convention for domain object properties like displayName, description etc...</li></ul>

<code>Hint</code> is an enum which contains few frequently used hints like <code>DISPLAY_NAME</code>, <code>DESCRIPTION</code> etc<br>
<br>
Let us say if you want to have how own hint.<br>
For example, all values user specified are trimmed and then set into domain object.<br>
but you don't want password field to be trimmed. then you can do:<br>
<pre><code>public static final String PROP_PASSWD = "passwd";<br>
@Bundle({<br>
    @Entry(hint=Hint.DISPLAY_NAME, rhs="Password"),<br>
    @Entry(hintName="doNotTrim", rhs="true")<br>
})<br>
private String passwd;<br>
</code></pre>
<pre><code>String value = passwdField.getText();<br>
if(Boolean.parseBoolean(I18n.getHint(Employee.class, Employee.PROP_PASSWD, "doNotTrim")))<br>
    value = value.trim();<br>
employee.setPasswd(value);<br>
</code></pre>
<b>NOTE:</b> currently <code>Hint</code> enum has very few hints, if you have any useful hints in your mind, then<br>
let me know, I will add them.<br>
<br>
<hr />
<h3>One Time used Property</h3>

Let us say in Employee registration gui form, you ask user to enter his/her password twice.<br>

<pre><code>public class EmployeeForm extends JDialog{<br>
    ...<br>
    @Bundle(@Entry(lhs="PASSWORD_MISMATCH", rhs="Paswords specified doesn't math"))<br>
    private void onOK(){<br>
        ...<br>
        String passwd1 = password1.getText();<br>
        String passwd2 = password2.getText();<br>
        if(!passwd1.equals(passwd2)){<br>
            JOptionPane.showMessageDialog(this, I18N.getMessage(EmployeeForm.class, "PASSWORD_MISMATCH");<br>
            return;<br>
        }<br>
        ...<br>
    }<br>
    ...<br>
}    <br>
</code></pre>

The advantage of this is when you delete this method, the property is also deleted<br>
i.e, you no longer need to worry about having unused properties.<br>
<br>
you can also add comments to properties as below:<br>
<pre><code>@Bundle({<br>
    @Entry(" {0} applicationName ==&gt; Name of Application"),<br>
    @Entry(" {1} applicationVersion ==&gt; Version of Application"),<br>
    @Entry(lhs="APP_NOT_FOUND", rhs="cannot find application {0} of version {1}")<br>
})<br>
public void launchApplication(String appName, int version){<br>
    ...<br>
    throw new RuntimeException(I18N.getMessage(Launcher.class, "APP_NOT_FOUND", appName, version));<br>
}<br>
</code></pre>

Your comments are appreciated;
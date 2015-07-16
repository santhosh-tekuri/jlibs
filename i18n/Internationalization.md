---
title: Internationalization made easier
layout: default
---

# Internationalization made easier

In traditional approach, we create properties file;  
But with JLibs you create an interface and annotate it with `@ResourceBundle`  

## Dependencies ###

```xml
<dependency>
    <groupId>jlibs</groupId>
    <artifactId>jlibs-i18n</artifactId>
    <version>2.0</version>
</dependency> 

<dependency>
    <groupId>jlibs</groupId>
    <artifactId>jlibs-i18n-apt</artifactId>
    <version>2.0</version>
    <optional>true</optional>
</dependency> 
```

`jlibs-i18n-apt` contains annotation processor and is required only at *compile time*

## Eclipse ##

Eclipse does not do automatic annotation processing from classpath currently. See [Bug 280542](https://bugs.eclipse.org/bugs/show_bug.cgi?id=280542)

So you need to manually configure this as below:

- open the project properties dialog, and go to the Java Compiler / Annotation Processing panel
- check "Enable Project Specific Settings" and "Enable Annotation Processing"
- set "Generated source directory" to "target/generated-sources/annotations"
- now open the Java Compiler / Annotation Processing / Factory Path panel
- add jlibs-apt.jar, jlibs-core.jar, jlibs-i18n.jar and jlibs-i18-apt.jar (you can get these jars from ~/.m2 folder)
- now build the project

## Sample Code ##

```java
import jlibs.core.util.i18n.I18N;
import jlibs.core.util.i18n.Message;
import jlibs.core.util.i18n.ResourceBundle;

@ResourceBundle
public interface DBBundle{
    public static final DBBundle DB_BUNDLE = I18N.getImplementation(DBBundle.class);
    
    @Message("SQL Execution completed in {0} seconds with {1} errors")
    public String executionFinished(long seconds, int errorCount);

    @Message(key="SQLExecutionException", value="Encountered an exception while executing the following statement:\n{0}")
    public String executionException(String query);

    @Message("executing {0}")
    public String executing(String query);
}
```

let us walk through code:

```java
@ResourceBundle
public interface DBBundle{
```

`@ResourceBundle` says that this interface is used for I18N purpose. This annotation can be applied only on interface.

all methods in this interface should be annotated with `@Message`.  
For each message you want, you will add a method in this interface

```java
@Message("SQL Execution completed in {0} seconds with {1} errors")
public String executionFinished(long seconds, int errorCount);
```

here the key of message is the name of the method. i.e, `executionFinished`  
and the value of message is `SQL Execution completed in {0} seconds with {1} errors`

```java
@Message(key="SQLExecutionException", value="Encountered an exception while executing the following statement:\n{0}")
public String executionException(String query);
```
here we are explicitly specifying key as `SQLExecutionException`

When you compile this interface with `jlibs-core.jar` in classpath, it will generate:

- `Bundle.properties` which contains the messages
- `_Bundle.class` which implements your interface and each method implementation returns its corresponding message from `Bundle.properties`

```java
public static final DBBundle DB_BUNDLE = I18N.getImplementation(DBBundle.class);
```
`I18N.getImplementation(DBBundle.class)` returns an instance of `_Bundle` class that is generated.

You can have more than one interface with `@ResourceBundle` in a package. In such case:

- generated `Bundle.properties` will have messages from all interfaces
- generated `_Bundle.class` will be implementing all these interfaces

i.e it would be easier to group messages based on the context they are used.
Let us say I have UIBundle interface in same package, which contains messages used by UI:

```java
@ResourceBundle
public interface UIBundle{
    public static final UIBundle UI_BUNDLE = I18N.getImplementation(UIBundle.class);

    @Message("Execute")
    public String executeButton();
    
    @Message("File {0} already exists.  Do you really want to replace it?")
    public String confirmReplace(File file);
}
```

`DBBundle` contains all messages used in database interaction  
`UIBundle` contains all messages used by UI classes

let us see sample code using these bundles:

```java
import static i18n.DBBundle.DB_BUNDLE;
import static i18n.UIBundle.UI_BUNDLE;

executeButton.setText(UI_BUNDLE.executeButton());

try{
    System.out.println(DB_BUNDLE.executing(query));
    // execute query
    System.out.println(DB_BUNDLE.executionFinished(5, 0));
}catch(SQLException ex){
    System.out.println(DB_BUNDLE.executionException(query));
}
```

You can see that, the code looks clean without any hardcoded message keys.

**Tip:**
If you replace `I18N.getImplementation(UIBundle.class)` with `_Bundle.INSTANCE`, then
you dont even need to add `jlibs-i18n` dependency. i.e, your project has no runtime
dependencies on jlibs.

## Documentation ##

```java
@Message("SQL Execution completed in {0} seconds with {1} errors")
public String executionFinished(long seconds, int errorCount);
```

the message generated in `Bundle.properties` will be:

```properties
# {0} seconds
# {1} errorCount
executionFinished=SQL Execution completed in {0} seconds with {1} errors
```

the generated message tells what `{0}` and `{1}`` are referring to.  
This makes the job of translator (who is translating to some other language) easier, because he/she now understand the message better.

```java
/**
  * thrown when failed to load application
  * because of network failure
  *
  * @param application   UID of application
  * @param version       version of the application
*/
@Message(key = "cannotKillApplication", value="failed to kill application {0} with version {1}")
public String cannotKillApplication(String application, String version);
```

the message generated in `Bundle.properties` will be:

```properties
# thrown when failed to load application
# because of network failure
# {0} application ==> UID of application
# {1} version ==> version of the application
cannotKillApplication=failed to kill application {0} with version {1}
```

i.e, any additional javadoc specified is also made available in generated `Bundle.properties`.  
This makes the job of translator more comfortable.

`Bundle.properties` generated for DBBundle, UIBundle will look as below:

```properties
# DON'T EDIT THIS FILE. THIS IS GENERATED BY JLIBS
# @author Santhosh Kumar T

#-------------------------------------------------[ DBBundle ]---------------------------------------------------

# {0} query
executing=executing {0}

# {0} query
SQLExecutionException=Encountered an exception while executing the following statement:{0}

# {0} seconds
# {1} errorCount
executionFinished=SQL Execution completed in {0} seconds with {1} errors

#-------------------------------------------------[ UIBundle ]---------------------------------------------------

executeButton=Execute

# {0} file
confirmReplace=File {0} already exists.  Do you really want to replace it?
```

You can see that the messages from each interface are clearly separated in genrated properties file

## Developer/IDE Friendly ##

```java
import static i18n.DBBundle.DB_BUNDLE;
import static i18n.UIBundle.UI_BUNDLE;

executeButton.setText(UI_BUNDLE.executeButton());

try{
    System.out.println(DB_BUNDLE.executing(query));
    // execute query
    System.out.println(DB_BUNDLE.executionFinished(5, 0));
}catch(SQLException ex){
    System.out.println(DB_BUNDLE.executionException(query));
}
```

the code using I18N messages is no longer cluttered with hardcoded strings. you never need to fear of:

- misspelling message keys
- specifying wrong number of arguments to message
- specifying arguments in incorrect order to message

i.e you get complete compile-time safety, and IDE help, because messages are now java methods rather than hard-coded Strings

## Invalid Messge Formats ##

```java
@Message("your lass successfull login is on {0, timee}")
public String lastSucussfullLogin(Date date);<br>
```

here we misspelled the format `time` as `timee`  
this will give following compile time error:

```
[javac] /jlibsuser/src/i18n/UIBundle.java:23: Invalid Message Format: unknown format type at 
[javac]     @Message("your lass successfull login is on {0, timee}")
[javac]     ^
```
i.e any invalid message formats are caught at compile time

## Argument Count Mismatch ##

```java
@Message("SQL Execution completed in {0} seconds with {1} errors")
public String executionFinished(long seconds);
```

here the message requires two arguments `{0}` and `{1}`. but the java method is taking only one argument.  
this will give following compile time error:

```
[javac] /jlibsuser/src/i18n/DBBundle.java:15: no of args in message format doesn't match with the number of parameters this method accepts
[javac]     public String executionFinished(long seconds);
[javac]                   ^
```

## Missing Argument ##

```java
@Message("SQL Execution completed in {0} seconds with {2} errors and {2} warnings")
public String executionFinished(long seconds, int errorCount, int warningCount);
```

here we misspelled `{1} errros` as `{2} errors`.  
this will give following compile time error:

```
[javac] /jlibsuser/src/i18n/DBBundle.java:14: {1} is missing in message
[javac]     @Message("SQL Execution completed in {0} seconds with {2} errors and {2} warnings")
[javac]     ^
```

## Duplicate Key ##

```java
@Message(key="JLIBS015", value="SQL Execution completed in {0} seconds with {1} errors and {2} warnings")
public String executionFinished(long seconds, int errorCount, int warningCount);

@Message(key="JLIBS015", value="Encountered an exception while executing the following statement:\n{0}")
public String executionException(String query);
```

here we accidently used same key <code>JLIBS015</code> for both methods.  
this will give following compile time error:

```
[javac] /jlibsuser/src/i18n/DBBundle.java:18: key 'JLIBS015' is already used by "java.lang.String executionFinished(long, int, int)" in i18n.DBBundle interface
[javac]     public String executionException(String query);
[javac]                   ^
```

## Method Signature Clash ##

```java
public interface DBBundle{
    ...
    @Message(key="EXECUTING", value="executing {0}")
    public String executing(String query);
    ...
}

public interface UIBundle{
    ...
    @Message(key="EXECUTING_QUERY", value="executing {0}")
    public String executing(String query);
    ...
}
```

here both `DBBundle` and `UIBundle` are in same package and has methods with identical signature.  
The generated `_Bundle` class implements both the interfaces `DBBundle` and `UIBundle`,  
so it can't decide whether to use key `EXECUTING` or `EXECUTING_QUERY`  
thus this will give following compile time error:

```
[javac] /jlibsuser/src/i18n/UIBundle.java:27: clashes with similar method in i18n.DBBundle interface
[javac]     public String executing(String query);
[javac]                   ^
```

## Optimization ##

All annotations have source level retention policy. So there is no reflection used at runtime.

Only one `_Bundle.java` class is generated per package, and this class will implement all interfaces with `@ResourceBundle` annatation in that package

there is only one instance of `_Bundle` created by `I18N.getImplementation(clazz)`  
i.e both `DBBundle.DB_BUNDLE` and `UIBundle.UI_BUNDLE` are referring to same instanceof `_Bundle`.

the `_Bundle` class caches the `ResourceBundle` loaded.

## Customization ##

You can change the name of the properties file generated by passing `-AResourceBundle.basename=MyBundle` to `javac`.  
this will create `MyBundle.properties`

## ErrorCodes ##

If you application uses error codes, as below:

```java
package com.foo.myapp;

public class UncheckedException extends RuntimeException{
    private String errorCode;

    public UncheckedException(String errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode(){
        return errorCode;
    }

    @Override
    public String toString(){
        String s = getClass().getName()+": "+errorCode;
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
```

and in you application you always throw <code>UncheckedException</code> with different errorcode.

In such case, you can internationalize errorcodes as below:

```java
package com.foo.myapp.controllers;

public interface ErrorCodes{
    public static final ErrorCodes INSTANCE = I18N.getImplementation(ErrorCodes.class);
    
    @Message("Database connection to host {0} is lost")
    public UncheckedException connectionLost(String host);
    
    @Message("No book found titled {0} from author {1}")
    public UncheckedException noSuchBookFound(String title, String author);
}
```

now you can throw `UncheckedException` as follows:

```java
throw ErrorCodes.INSTANCE.connectionList(host);
throw ErrorCodes.INSTANCE.noSuchBookFound(title, author);
```

the exception class returned in `ErrorCodes` should have a constructor taking `errorCode` and `message` as arguments. The errorCode generated for `connectionLost` will be `myapp.controllers.ConnectionList`. i.e, the package and method name are joined. the top two packages are ignored and the first letter of method name is changed to uppercase.

you can configure the number of top pakcages to be ignored by passing following option to annotation processor from javac:  
`-AResourceBundle.ignorePackageCount=3`

The default value is `2`.  
If you want complete package name, then use value of `0`.  
If you do not want package name in errorcode, then use value of `-1`

## Internationalizing Domain Objects ##

Rather than internationalizing GUI, I would recomment internationalizing your domain objects.

let us say your domain object is `Employee`:

```java
import jlibs.core.util.i18n.*;

public class Employee{<
    public static final String PROP_NAME = "name";
    public static final String PROP_AGE  = "age";
    
    @Bundle({
        @Entry(hint=Hint.DISPLAY_NAME, rhs="User Name"),
        @Entry(hint=Hint.DESCRIPTION, rhs="Full Name of Employee")
    })
    private String name;

    @Bundle({
        @Entry(hint=Hint.DISPLAY_NAME, rhs="Age"),
        @Entry(hint=Hint.DESCRIPTION, rhs="Current Age of Employee")
    })
    private int age;
    
    // getter and setter methods<br>
}
```

here we are internationalizing each property of employee using `@Bundle` annotation.

```java
@Bundle({
    @Entry(hint=Hint.DISPLAY_NAME, rhs="User Name"),
    @Entry(hint=Hint.DESCRIPTION, rhs="Full Name of Employee")
})
private String name;
```

this will create following in `Bundle.properties`:

```properties
Employee.name.displayName=User Name
Employee.name.descritpion=Full Name of Employee
```

i.e each property generated is qualified by the class and field.

now in Employee registration form, you can do:

```java
import jlibs.core.util.i18n.*;

JLabel nameLabel = ...;
JTextField nameField = ...;
nameLabel.setText(Hint.DISPLAY_NAME.stringValue(Employee.class, Employee.PROP_NAME));
nameField.setTooltipText(Hint.DESCRIPTION.stringValue(Employee.class, Employee.PROP_NAME));
```

let us say you have a JTable listing all employees, then you can do:

```java
import jlibs.core.util.i18n.*;

Vector columnNames = new Vector();
columnNames.add(Hint.DISPLAY_NAME.stringValue(Employee.class, Employee.PROP_NAME));
columnNames.add(Hint.DISPLAY_NAME.stringValue(Employee.class, Employee.PROP_AGE));
JTable table = new JTable(employees, columnNames);
```

Here we used same properties in two GUI Panes. We used same properties in both GUI to internationalize it.

Moving internationalization from GUI to Domain Objects, allows:

- reusable properties
- a convention for domain object properties like displayName, description etc...

`Hint` is an enum which contains few frequently used hints like `DISPLAY_NAME`, `DESCRIPTION` etc

Let us say if you want to have how own hint.  
For example, all values user specified are trimmed and then set into domain object.  
but you don't want password field to be trimmed. then you can do:  

```java
public static final String PROP_PASSWD = "passwd";
@Bundle({
    @Entry(hint=Hint.DISPLAY_NAME, rhs="Password"),
    @Entry(hintName="doNotTrim", rhs="true")
})
private String passwd;
```

```java
String value = passwdField.getText();
if(Boolean.parseBoolean(I18n.getHint(Employee.class, Employee.PROP_PASSWD, "doNotTrim")))
    value = value.trim();
employee.setPasswd(value);
```
<b>NOTE:</b> currently `Hint` enum has very few hints, if you have any useful hints in your mind, then simply raise 
an issue, I will add them.

## One Time used Property ##

Let us say in Employee registration gui form, you ask user to enter his/her password twice.

```java
public class EmployeeForm extends JDialog{
    ...
    @Bundle(@Entry(lhs="PASSWORD_MISMATCH", rhs="Paswords specified doesn't math"))
    private void onOK(){
        ...
        String passwd1 = password1.getText();
        String passwd2 = password2.getText();
        if(!passwd1.equals(passwd2)){
            JOptionPane.showMessageDialog(this, I18N.getMessage(EmployeeForm.class, "PASSWORD_MISMATCH");
            return;
        }
        ...
    }
    ...
}
```

The advantage of this is when you delete this method, the property is also deleted  
i.e, you no longer need to worry about having unused properties.

you can also add comments to properties as below:

```java
@Bundle({
    @Entry(" {0} applicationName ==> Name of Application"),
    @Entry(" {1} applicationVersion ==> Version of Application"),
    @Entry(lhs="APP_NOT_FOUND", rhs="cannot find application {0} of version {1}")
})
public void launchApplication(String appName, int version){
    ...
    throw new RuntimeException(I18N.getMessage(Launcher.class, "APP_NOT_FOUND", appName, version));
}
```

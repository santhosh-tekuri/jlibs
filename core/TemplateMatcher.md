---
title: TemplateMatcher
layout: default
---

# Simple Template Engine #

`TemplateMatcher` is a simple template engine provided with jlibs.

~~~java
import jlibs.core.util.regex.TemplateMatcher;

String msg = "Hai ${user}, your mail to ${email} has been sent successfully.";
TemplateMatcher matcher = new TemplateMatcher("${", "}");
Map<String, String> vars = new HashMap<String, String>();
vars.put("user", "santhosh");
vars.put("email", "scott@gmail.com");
System.out.println(matcher.replace(msg, vars));
~~~

prints following:

~~~
Hai santhosh, your mail to scott@gmail.com has been sent successfully.
~~~

The two arguments to `TemplateMatcher` are leftBrace and rightBrace.

For example:

~~~java
String msg = "Hai ___user___, your mail to ___email___ has been sent successfully.";
TemplateMatcher matcher = new TemplateMatcher("___", "___");
Map<String, String> vars = new HashMap<String, String>();
vars.put("user", "santhosh");
vars.put("email", "scott@gmail.com");
System.out.println(matcher.replace(msg, vars));
~~~

also prints the same output;


**NOTE:** if a variables resolves to null, then it appears as it is in result string

Right Brace is optional:

~~~java
String msg = "Hai $user, your mail to $email has been sent successfully.";
TemplateMatcher matcher = new TemplateMatcher("$");
Map<String, String> vars = new HashMap<String, String>();
vars.put("user", "santhosh");
vars.put("email", "scott@gmail.com");
System.out.println(matcher.replace(msg, vars));
~~~

also prints the same output;

## Variable Resolution ##

you can also define custom variable resolution:

~~~java
String msg = "Hai ${user.name}, you are using JVM from ${java.vendor}.";
TemplateMatcher matcher = new TemplateMatcher("${", "}");<br>
String result = matcher.replace(msg, new TemplateMatcher.VariableResolver(){
    @Override
    public String resolve(String variable){
        return System.getProperty(variable);
    }
});
~~~

prints:

~~~
Hai santhosh, you are using JVM from Apple Inc..
~~~

`VariableResolver` interface contains single method:

~~~java
public String resolve(String variable)
~~~

## Using with writers ##

let us say you have file `template.txt` which contains:

~~~
Hai ${user},
    your mail to ${email} has been sent successfully.
~~~

running the following code:

~~~java
TemplateMatcher matcher = new TemplateMatcher("${", "}");
Map<String, String> vars = new HashMap<String, String>();
vars.put("user", "santhosh");
vars.put("email", "scott@gmail.com");
matcher.replace(new FileReader("templte.txt"), new FileWriter("result.txt"), vars);
~~~

will creates file `result.txt` with following content:

~~~
Hai santhosh,
    your mail to scott@gmail.com has been sent successfully.
~~~

## Copying Files/Directories ##

`TemplateMatcher` provides method to copy files/directories:

~~~java
public void copyInto(File source, File targetDir, Map<String, String> variables) throws IOException;
~~~

Name of each file and directory is treated as a template.  
If name of directory is `${xyz}` after applying template, if resolves to `a/b/c`,  
then it expands into the directory structure `a/b/c`.

for example we have following directory structure:

~~~java
${root}
  |- ${class}.java
~~~

and content of `${class}.java` file is:

~~~java
package ${rootpackage};

public class ${class} extends Comparator{

}
~~~

now running following code:

~~~java
TemplateMatcher matcher = new TemplateMatcher("${", "}");
Map<String, String> vars = new HashMap<String, String>();
vars.put("root", "org/example");
vars.put("rootpackage", "org.example");
vars.put("class", "MyClass");
matcher.copyInto(new File("${root}"), new File("."), vars);
~~~

creates:

~~~
org
 |-example
    |-MyClass.java
~~~

and content of `MyClass.java` will be:

~~~java
package org.example;

public class MyClass extends Comparator{

}
~~~

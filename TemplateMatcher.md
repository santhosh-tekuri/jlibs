`TemplateMatcher` is a simple template engine provided with jlibs.

```
import jlibs.core.util.regex.TemplateMatcher;

String msg = "Hai ${user}, your mail to ${email} has been sent successfully.";

TemplateMatcher matcher = new TemplateMatcher("${", "}");

Map<String, String> vars = new HashMap<String, String>();
vars.put("user", "santhosh");
vars.put("email", "scott@gmail.com");
System.out.println(matcher.replace(msg, vars));
```
prints following:
```
Hai santhosh, your mail to scott@gmail.com has been sent successfully.
```

The two arguments to `TemplateMatcher` are leftBrace and rightBrace.<br>
For example:<br>
<pre><code>String msg = "Hai ___user___, your mail to ___email___ has been sent successfully.";<br>
TemplateMatcher matcher = new TemplateMatcher("___", "___");<br>
<br>
Map&lt;String, String&gt; vars = new HashMap&lt;String, String&gt;();<br>
vars.put("user", "santhosh");<br>
vars.put("email", "scott@gmail.com");<br>
System.out.println(matcher.replace(msg, vars));<br>
</code></pre>
also prints the same output;<br>
<br>
<b>NOTE:</b>
<ul><li>if a variables resolves to null, then it appears as it is in result string</li></ul>

Right Brace is optional. in such case use <code>new TemplateMatcher(leftBrace)</code>:<br>
<pre><code>String msg = "Hai $user, your mail to $email has been sent successfully.";<br>
<br>
TemplateMatcher matcher = new TemplateMatcher("$");<br>
<br>
Map&lt;String, String&gt; vars = new HashMap&lt;String, String&gt;();<br>
vars.put("user", "santhosh");<br>
vars.put("email", "scott@gmail.com");<br>
System.out.println(matcher.replace(msg, vars));<br>
</code></pre>
also prints the same output;<br>
<br>
<b>Variable Resolution:</b>

you can also resolve variables dynamically:<br>
<pre><code>String msg = "Hai ${user.name}, you are using JVM from ${java.vendor}.";<br>
<br>
TemplateMatcher matcher = new TemplateMatcher("${", "}");<br>
String result = matcher.replace(msg, new TemplateMatcher.VariableResolver(){<br>
    @Override<br>
    public String resolve(String variable){<br>
        return System.getProperty(variable);<br>
    }<br>
});<br>
</code></pre>
prints<br>
<pre><code>Hai santhosh, you are using JVM from Apple Inc..<br>
</code></pre>

<code>VariableResolver</code> interface contains single method:<br>
<pre><code>public String resolve(String variable)<br>
</code></pre>

<b>Using with writers:</b>

let us say you have file <code>template.txt</code> which contains:<br>
<pre><code>Hai ${user},<br>
    your mail to ${email} has been sent successfully.<br>
</code></pre>
running the following code:<br>
<pre><code>TemplateMatcher matcher = new TemplateMatcher("${", "}");<br>
<br>
Map&lt;String, String&gt; vars = new HashMap&lt;String, String&gt;();<br>
vars.put("user", "santhosh");<br>
vars.put("email", "scott@gmail.com");<br>
matcher.replace(new FileReader("templte.txt"), new FileWriter("result.txt"), vars);<br>
</code></pre>
will creates file <code>result.txt</code> with following content:<br>
<pre><code>Hai santhosh,<br>
    your mail to scott@gmail.com has been sent successfully.<br>
</code></pre>

<b>Copying Files/Directories:</b>

<code>TemplateMatcher</code> provides method to copy files/directories:<br>
<pre><code>public void copyInto(File source, File targetDir, Map&lt;String, String&gt; variables) throws IOException;<br>
</code></pre>

Name of each file and directory is treated as a template.<br>
If name of directory is <code>${xyz}</code> after applying template, if resolves to <code>"a/b/c"</code>,<br>
then it expands into the directory structure <code>a/b/c</code>;<br>
<br>
for example we have following directory structure:<br>
<br>
<pre><code>${root}<br>
  |- ${class}.java<br>
</code></pre>
and content of <code>${class}.java</code> file is:<br>
<pre><code>package ${rootpackage};<br>
<br>
public class ${class} extends Comparator{<br>
<br>
}<br>
</code></pre>

now running following code:<br>
<pre><code>TemplateMatcher matcher = new TemplateMatcher("${", "}");<br>
<br>
Map&lt;String, String&gt; vars = new HashMap&lt;String, String&gt;();<br>
vars.put("root", "org/example");<br>
vars.put("rootpackage", "org.example");<br>
vars.put("class", "MyClass");<br>
<br>
matcher.copyInto(new File("${root}"), new File("."), vars);<br>
</code></pre>

creates:<br>
<pre><code>org<br>
 |-example<br>
    |-MyClass.java<br>
</code></pre>
and content of <code>MyClass.java</code> will be:<br>
<pre><code>package org.example;<br>
<br>
public class MyClass extends Comparator{<br>
<br>
}<br>
</code></pre>
---
title: OSInformation
layout: default
---

# Find OS on which jvm is running #

[jlibs.core.lang.OS](https://github.com/santhosh-tekuri/jlibs/blob/master/core/src/main/java/jlibs/core/lang/OS.java) is an enum.

This enum contains values for each type of OS. for example: `WINDOWS_NT`, `WINDOWS_98`, `LINUX`, `SOLARIS`, `MAC` etc...

~~~java
import jlibs.core.lang.OS;

OS myos = OS.get();
System.out.println(myos);
~~~

On my laptop, it prints `MAC`

you can also check whether your os is windows or unix:

~~~java
OS myos = OS.get();
System.out.println("isWindows: "+myos.isWindows());
System.out.println("isUnix: "+myos.isUnix());
~~~

On my laptop, it prints:

~~~
isWindows: false
isUnix: true
~~~

When your OS is not recognized, `OS.get()` returns `OS.OTHER`

There is another usefult method which might be required rarely.

~~~java
String osName = System.getProperty("os.name");
OS os = OS.get(osName);
~~~

This might be handy, if your app is distributed and and want to find out the os of other JVM process

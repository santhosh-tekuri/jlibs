This project contains various common utilities which become handy in daily development of java projects.

This project has various modules. Following are the utilities available per module:

  * Core
    1. [Find OS on which jvm is running](OSInformation.md)
    1. [Computing hashcode](hashCode.md)
    1. [Simple Template Engine](TemplateMatcher.md)
    1. [Garbage Collection & Finalization](GarbageCollection.md)
    1. [Ansi Colors in Console Output](AnsiColoring.md)
    1. [Comparators](Comparators.md)
    1. [Pumping Input/Output](PumpingIO.md)
    1. [Launching Java Process Programatically](JavaProcessBuilder.md)
    1. [Internationalization made easier](Internationalization.md)
    1. [Counting by Units](Counting.md)
  * XML
    1. [Working with Namespaces](Namespaces.md)
    1. [Creating XML using SAX](XMLDocument.md)
    1. [Replacing XML Namespaces](NamespaceReplacer.md)
    1. [SAX-JAVA Binding Made Easier](SAX2JavaBinding.md)
    1. [Crawling XML(wsdl, xsd, xsl) Documents](XMLCrawler.md)
    1. [Generating Sample XML for given XMLSchema](XSInstance.md)
    1. [NON-Blocking XMLReader](AsyncXMLReader.md)
  * Swing
    1. [Avoid code cluttering when interacting with EDT](EventDispatchThread.md)
  * [DAO Pattern](DAOPattern.md) - J2EE DAO Pattern made easier ![http://jlibs.googlecode.com/svn/trunk/wiki/updated.gif](http://jlibs.googlecode.com/svn/trunk/wiki/updated.gif)
  * [XMLDog](XMLDog.md) - SAX based XPath 1.0 Engine
  * [JLaunch](JLaunch.md) - An easy approach for maintaining launching scripts
  * [GrepLog](GrepLog.md) - Command line grep tool for log files

**For Maven Users**
```
<dependency>
    <groupId>jlibs</groupId>
    <artifactId>jlibs-jdbc</artifactId>
    <version>1.0</version>
</dependency> 

<repository>
    <id>jlibs-repository</id>
    <name>JLibs Repository</name>
    <url>http://dl.dropbox.com/u/326301/repository</url>
    <layout>default</layout>
</repository>
```

```
<dependency>
    <groupId>jlibs</groupId>
    <artifactId>jlibs-jdbc</artifactId>
    <version>1.1-SNAPSHOT</version>
</dependency> 

<repository>
    <id>jlibs-snapshots-repository</id>
    <name>JLibs Snapshots Repository</name>
    <url>http://dl.dropbox.com/u/326301/snapshots</url>
    <layout>default</layout>
</repository>
```
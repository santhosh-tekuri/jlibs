---
title: JLibs
layout: default
---

This project is licensed under the terms of the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Maven

~~~xml
<dependency>
    <groupId>in.jlibs</groupId>
    <artifactId>jlibs-XXX</artifactId>
    <version>2.2.2</version>
</dependency> 

~~~

replace `jlibs-XXX` with the module you want to use. For example: `jlibs-xmldog`

## Build

the `toolchains.xml` file (see below) should be put in `$user.home/.m2` directory

~~~xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>1.8</version>
        </provides>
        <configuration>
            <jdkHome>/path/to/jdk/1.8</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
~~~

modify `jdkPath` in above xml appropriately

to build:

~~~
$ git clone https://github.com/santhosh-tekuri/jlibs.git
$ cd jlibs
$ mvn
~~~

after successful build, you can find distribution at `distribution/target/jlibs-2.2.2`

## Feedback and Collaboration ##

- For bugs and enhancements, please use [Github Issues](https://github.com/santhosh-tekuri/jlibs/issues)
- For questions and feedback, please email [santhosh.tekuri@gmail.com](mailto:santhosh.tekuri@gmail.com)

## User Guide

### jlibs-core: ###

* [Garbage Collection & Finalization](core/GarbageCollection.html)
* [Find OS on which jvm is running](core/OSInformation.html)
* [Counting by Units](core/Counting.html)
* [Simple Template Engine](core/TemplateMatcher.html)

### jlibs-i18n: ###

* [Internationalization made easier](i18n/Internationalization.html)

### jlibs-xml: ###

* [Working with Namespaces](xml/Namespaces.html)
* [Replacing XML Namespaces](xml/NamespaceReplacer.html)
* [Creating XML using SAX](xml/XMLDocument.html)

### jlibs-xml-nbpi: ###

* [NON-Blocking XMLReader](xml/nbp/AsyncXMLReader.html)

### jlibs-xml-cralwer: ###

* [Crawl XML(wsdl, xsd, xsl) documents](xml/crawler/XMLCrawler.html)

### jlibs-xml-binding: ###

* [SAX-JAVA Binding Made Easier](xml/binding/SAX2JavaBinding.html)

### jlibs-xsd: ###

* [Generate sample XML from XML-Schema](xsd/XSInstance.html)

### jlibs-xmldog: ###

* [SAX based XPath 1.0 engine](xmldog/XMLDog.html)

### jlibs-jdbc: ###

* [J2EE DAO Pattern made easier](jdbc/DAOPattern.html)

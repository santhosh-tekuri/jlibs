#! /bin/sh ../launcher/jlaunch.sh

<java.classpath>
../core/jlibs-core.jar
../xml/lib/xercesImpl.jar
../xml/jlibs-xml.jar
lib/jaxen-1.1.1.jar
jlibs-xmldog.jar
jlibs-xmldog-test.jar

# ensure that these jare are downloaded
../3rdparty/saxon9.jar
../3rdparty/saxon9-dom.jar
../3rdparty/saxon9-xpath.jar

<jvm.args>
-ea
jlibs.xml.sax.dog.tests.XPathPerformanceTest
resources/xpaths.xml

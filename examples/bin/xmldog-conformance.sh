#!/bin/sh

JLIBS_HOME=`dirname $0`
JLIBS_HOME=`cd $JLIBS_HOME/..;pwd`

if [ ! -f "$JLIBS_HOME/lib/external/saxon.jar" ] ; then
    echo please download http://svn.apache.org/repos/asf/tuscany/maven/net/sf/saxon/saxon/9.0.0.2/saxon-9.0.0.2.jar and save as $JLIBS_HOME/lib/external/saxon.jar
    exit
fi
if [ ! -f "$JLIBS_HOME/lib/external/saxon-dom.jar" ] ; then
    echo please download http://svn.apache.org/repos/asf/tuscany/maven/net/sf/saxon/saxon-dom/9.0.0.2/saxon-dom-9.0.0.2.jar and save as $JLIBS_HOME/lib/external/saxon-dom.jar
    exit
fi
if [ ! -f "$JLIBS_HOME/lib/external/saxon-xpath.jar" ] ; then
    echo please download http://svn.apache.org/repos/asf/tuscany/maven/net/sf/saxon/saxon-xpath/9.0.0.2/saxon-xpath-9.0.0.2.jar and save as $JLIBS_HOME/lib/external/saxon-xpath.jar
    exit
fi

`dirname $0`/launcher/jlaunch.sh $0
exit

#---------------------[ Configuration ]--------------------

<java.classpath>
../lib/jlibs-core.jar

../lib/external/xercesImpl.jar
../lib/jlibs-xml.jar

../lib/external/jaxen.jar
../lib/jlibs-xmldog.jar

../lib/external/saxon.jar
../lib/external/saxon-dom.jar
../lib/external/saxon-xpath.jar
../lib/jlibs-examples.jar

<jvm.args>
-ea
jlibs.xml.sax.dog.tests.XPathConformanceTest

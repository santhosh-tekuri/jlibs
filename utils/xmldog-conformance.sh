#!/bin/sh

CLASSPATH=3rdparty/annotations.jar:jlibs-core.jar
CLASSPATH=$CLASSPATH:3rdparty/xercesImpl.jar:$CLASSPATH:3rdparty/jaxen-1.1.1.jar:jlibs-xml.jar
CLASSPATH=$CLASSPATH:3rdparty/org-netbeans-swing-outline.jar:jlibs-swing.jar
CLASSPATH=$CLASSPATH:jlibs-utils.jar

java -classpath $CLASSPATH jlibs.xml.sax.sniff.XPathConformanceTest xpaths.xml

#!/bin/sh

CLASSPATH=3rdparty/annotations.jar:jlibs-core.jar
CLASSPATH=$CLASSPATH:3rdparty/xercesImpl.jar:jlibs-xml.jar
CLASSPATH=$CLASSPATH:3rdparty/org-netbeans-swing-outline.jar:jlibs-swing.jar
CLASSPATH=$CLASSPATH:jlibs-utils.jar

java -classpath $CLASSPATH jlibs.xml.xsd.XSDOutlinePanelTest

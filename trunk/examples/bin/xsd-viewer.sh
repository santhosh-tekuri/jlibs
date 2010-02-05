#!/bin/sh
`dirname $0`/launcher/jlaunch.sh $0
exit

#---------------------[ Configuration ]--------------------

<java.classpath>
../lib/jlibs-core.jar

../lib/external/xercesImpl.jar
../lib/jlibs-xml.jar

../lib/external/org-netbeans-swing-outline.jar
../lib/jlibs-swing.jar

../lib/jlibs-examples.jar

<jvm.args>
-ea
jlibs.xml.xsd.XSDOutlinePanelTest


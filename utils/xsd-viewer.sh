#! /bin/sh ../launcher/jlaunch.sh

<java.classpath>
../core/jlibs-core.jar

../xml/lib/xercesImpl.jar
../xml/jlibs-xml.jar

../swing/lib/org-netbeans-swing-outline.jar
../swing/jlibs-swing.jar

jlibs-utils.jar

<jvm.args>
-ea
jlibs.xml.xsd.XSDOutlinePanelTest

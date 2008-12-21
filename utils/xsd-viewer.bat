set CLASSPATH=3rdparty\annotations.jar;jlibs-core.jar
set CLASSPATH=%CLASSPATH%;3rdparty\xercesImpl.jar;jlibs-xml.jar
set CLASSPATH=%CLASSPATH%;3rdparty\org-netbeans-swing-outline.jar;jlibs-swing.jar
set CLASSPATH=%CLASSPATH%;jlibs-utils.jar

java -classpath %CLASSPATH% jlibs.xml.xsd.XSDOutlinePanelTest
pause

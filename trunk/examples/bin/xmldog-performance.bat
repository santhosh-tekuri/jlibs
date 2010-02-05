@ECHO OFF
SET APP=%~nx0
SET APP=%APP:~0,-4%
CALL launcher\jlaunch.bat %APP%.sh examples.conf jlibs.xml.sax.dog.tests.XPathPerformanceTest ../resources/xpaths.xml
PAUSE

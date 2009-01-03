The 3rdparty libraries used in jlibs are not included in this distribution.
you should download 3rdparty.zip from http://jlibs.googlecode.com/files/3rdparty.zip
and extract them into 3rdparty directory inside this distribution;

Dependencies:
-----------------------------------------------------------

jlibs-core.jar:
    3rdparty/annotations.jar (Intellij Idea 8.0.1)

jlibs-xml.jar:
    3rdparty/xercesImpl.jar (version 2.9.1)
    jlibs-core.jar

jlibs-swing.jar:
    3rdparty/org-netbeans-swing-outline.jar (Netbeans 6.5)
    jlibs-xml.jar

jlibs-util.jar:
    jlibs-swing.jar

dependencies are recursive.
i.e
    jlibs-xml.jar depends on jlibs-core.jar
    jlibs-core.jar depends on 3rdparty/annotations.jar
    ===>
        jlibs-xml.jar depends on 3rdparty/annotations.jar 
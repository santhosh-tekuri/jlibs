LICENSE: LGPL
JAVA   : 1.6+

The 3rdparty libraries used in jlibs are not included in this distribution.
Run the ant file in $JLIBS_HOME/lib/external to download them.

Dependencies:
+---------------+--------------+-----------------------------------------------+
| Module        | Dependencies | 3rdparty Libraries                            |
+---------------+--------------+-----------------------------------------------+
| jlibs-core    |              |                                               |
| jlibs-jdbc    | jlibs-core   |                                               |
| jlibs-xml     | jlibs-core   | xercelImpl.jar (version 2.9.1)                |
| jlibs-xmldog  | jlibs-xml    | jaxen.jar (version 1.1.1)                     |
| jlibs-swing   | jlibs-xml    | org-netbeans-swing-outline.jar (netbeans 6.5) |
| jlibs-examples| jlibs-jdbc   | saxon.jar (version 9.0.0.2)                   |
|               | jlibs-swing  | saxon-dom.jar (version 9.0.0.2)               |
|               |              | saxon-xpath.jar (version 9.0.0.2)             |
|               |              | commons-dbcp (version 1.4)                    |
|               |              | commons-pool (version 1.5.4)                  |
|               |              | mysql-connector-java (version 5.1.12)         |
+---------------+--------------+-----------------------------------------------+

Note: Dependencies are recursive.

If you have any doubts in using jlibs, contact me at santhosh.tekuri@gmail.com

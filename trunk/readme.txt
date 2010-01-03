Jlibs uses Java6.

The 3rdparty libraries used in jlibs are not included in this distribution.
You should download 3rdparty.zip from 
   http://code.google.com/p/jlibs/downloads/list

Dependencies:
--------------------------------------------------------------------------------
| Module        | Dependencies | 3rdparty Libraries                            |
|---------------|--------------|-----------------------------------------------|
| jlibs-core    |              |                                               |
| jlibs-xml     | jlibs-core   | xercelImpl.jar (version 2.9.1)                |
| jlibs-xmldog  | jlibs-xml    | jaxen-1.1.1.jar                               |
| jlibs-swing   | jlibs-xml    | org-netbeans-swing-outline.jar (netbeans 6.5) |
| jlibs-util    | jlibs-swing  |                                               |
--------------------------------------------------------------------------------

Note: Dependencies are recursive.

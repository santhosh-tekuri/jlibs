package jlibs.xml.sax;

import org.xml.sax.helpers.NamespaceSupport;
import jlibs.xml.Namespaces;
import jlibs.core.lang.Util;

/**
 * @author Santhosh Kumar T
 */
public class MyNamespaceSupport extends NamespaceSupport{
    private MyNamespaceSupport suggested;

    public MyNamespaceSupport(){
        this(Namespaces.SUGGESTED);
    }

    public MyNamespaceSupport(MyNamespaceSupport suggested){
        this.suggested = suggested;
    }

    public String findPrefix(String uri){
        if(uri==null)
            uri = "";
        String prefix = getPrefix(uri);
        if(prefix==null){
            if(Util.equals(uri, getURI("")))
                prefix = "";
        }
        return prefix;
    }

    public boolean declarePrefix(String uri){
        String prefix = suggested.findPrefix(uri);
        if(prefix==null){
            int i = 1;
            while(getURI(prefix="ns"+i)!=null)
                i++;
        }
        return declarePrefix(prefix, uri);
    }
}

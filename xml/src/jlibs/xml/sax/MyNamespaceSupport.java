package jlibs.xml.sax;

import org.xml.sax.helpers.NamespaceSupport;
import jlibs.xml.Namespaces;
import jlibs.core.lang.Util;

import java.util.Properties;

/**
 * @author Santhosh Kumar T
 */
public class MyNamespaceSupport extends NamespaceSupport{
    private Properties suggested;

    public MyNamespaceSupport(){
        this(Namespaces.getSuggested());
    }

    public MyNamespaceSupport(Properties suggested){
        this.suggested = suggested;
    }

    public void suggestPrefix(String prefix, String uri){
        suggested.put(prefix, uri);
    }

    public String findPrefix(String uri){
        if(uri==null)
            uri = "";
        String prefix = getPrefix(uri);
        if(prefix==null){
            String defaultURI = getURI("");
            if(defaultURI==null)
                defaultURI = "";
            if(Util.equals(uri, defaultURI))
                prefix = "";
        }
        return prefix;
    }

    public String declarePrefix(String uri){
        String prefix = findPrefix(uri);
        if(prefix==null){
            prefix = suggested.getProperty(uri);
            if(prefix==null){
                int i = 1;
                while(getURI(prefix="ns"+i)!=null)
                    i++;

            }
            declarePrefix(prefix, uri);
        }
        return prefix;
    }
}

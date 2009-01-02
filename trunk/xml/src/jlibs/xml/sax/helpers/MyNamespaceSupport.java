package jlibs.xml.sax.helpers;

import jlibs.core.lang.Util;
import jlibs.xml.Namespaces;
import org.xml.sax.helpers.NamespaceSupport;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.StringTokenizer;

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
            prefix = suggestPrefix(uri);
            declarePrefix(prefix, uri);
        }
        return prefix;
    }

    private String suggestPrefix(String uri){
        String prefix = suggested.getProperty(uri);
        if(prefix!=null)
            return prefix;
        
        try{
            URI _uri = new URI(uri);

            String path = _uri.getPath();
            StringTokenizer stok = new StringTokenizer(path, "/");
            while(stok.hasMoreTokens())
                prefix = stok.nextToken();
            if(prefix!=null){
                if(getURI(prefix)==null)
                    return prefix;
            }else{
                String host = _uri.getHost();
                if(host!=null){
                    stok = new StringTokenizer(host, ".");
                    String curPrefix = null;
                    while(stok.hasMoreTokens()){
                        prefix = curPrefix;
                        curPrefix = stok.nextToken();
                    }
                }                
                if(prefix!=null){
                    if(getURI(prefix)==null)
                        return prefix;
                }else
                    prefix = "ns";
            }
        }catch(URISyntaxException ignore){
            // xml spec doesn't guarantee that namespace uri should be valid uri
        }
        
        int i = 1;
        while(getURI(prefix+i)!=null)
            i++;
        return prefix+i;
    }

    public static void main(String[] args){
        MyNamespaceSupport ns = new MyNamespaceSupport();
        System.out.println(ns.declarePrefix("http://www.sonoasystems.com/schemas/2007/8/3/sci/"));
        System.out.println(ns.declarePrefix("http://www.sonoasystems.com/schemas/2007/8/7/sci/"));
        System.out.println(ns.declarePrefix("http://com"));
        System.out.println(ns.declarePrefix("http://google.org"));
    }
}

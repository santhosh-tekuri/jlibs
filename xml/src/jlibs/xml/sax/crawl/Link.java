package jlibs.xml.sax.crawl;

import jlibs.core.io.FileUtil;
import jlibs.core.net.URLUtil;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Stack;

/**
 * @author Santhosh Kumar T
 */
public abstract class Link{
    protected String extensions[];
    protected Stack<QName> path = new Stack<QName>();

    public Link(String... extensions){
        this.extensions = extensions;
    }

    public Link pushElement(String uri, String name){
        path.push(new QName(uri, name));
        return this;
    }

    public boolean matches(List<QName> path){
        return this.path.equals(path);
    }

    public File suggestFile(File sourceFile, String location) throws URISyntaxException{
        String fileName = URLUtil.suggestFile(new URI(location), extensions);
        File file = new File(sourceFile.getParentFile(), fileName);
        return FileUtil.findFreeFile(file);
    }
}

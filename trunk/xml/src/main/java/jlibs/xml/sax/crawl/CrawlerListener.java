package jlibs.xml.sax.crawl;

import java.io.File;
import java.net.URL;

/**
 * @author Santhosh Kumar T
 */
public interface CrawlerListener{
    public void saved(URL url, File file);
    public void skipped(URL url);
}

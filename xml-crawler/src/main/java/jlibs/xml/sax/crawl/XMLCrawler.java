/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.xml.sax.crawl;

import jlibs.core.io.ByteArrayOutputStream2;
import jlibs.core.io.FileNavigator;
import jlibs.core.io.FileUtil;
import jlibs.core.lang.ByteSequence;
import jlibs.core.lang.OS;
import jlibs.core.net.URLUtil;
import jlibs.xml.sax.SAXUtil;
import jlibs.xml.xsl.TransformerUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class XMLCrawler{
    private CrawlingRules rules;

    private Element current;
    private int depth;

    public XMLCrawler(){
        rules = CrawlingRules.defaultRules();
        crawled = new HashMap<URL, File>();
    }
    
    public XMLCrawler(CrawlingRules rules){
        this.rules = rules;
        crawled = new HashMap<URL, File>();
    }

    private XMLCrawler(XMLCrawler crawler){
        rules = crawler.rules;
        crawled = crawler.crawled;
        resolver = crawler.resolver;
    }

    private XMLFilterImpl xmlFilter = new XMLFilterImpl(){
        @Override
        public void startDocument() throws SAXException{
            current = rules.doc;
            super.startDocument();
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException{
            if(depth==0){
                Element elem = current.findChild(namespaceURI, localName);
                if(elem==null)
                    depth = 1;
                else{
                    current = elem;
                    try{
                        if(file==null && current.extension!=null)
                            setFile(listener.toFile(url, current.extension));
                        if(current.locationAttribute!=null){
                            String linkNamespace = null;
                            if(current.namespaceAttribute!=null)
                                linkNamespace = atts.getValue(current.namespaceAttribute.getNamespaceURI(), current.namespaceAttribute.getLocalPart());
                            String location = atts.getValue(current.locationAttribute.getNamespaceURI(), current.locationAttribute.getLocalPart());
                            if(location!=null){
                                location = resolveLink(linkNamespace, location);
                                URL targetURL = URLUtil.toURL(location);
                                File targetFile = null;
                                if(crawled!=null)
                                    targetFile = crawled.get(targetURL);
                                if(targetFile==null && listener.doCrawl(targetURL))
                                    targetFile = new XMLCrawler(XMLCrawler.this).crawl(new InputSource(location), listener, null);
                                String href = targetFile==null ? location : FileNavigator.INSTANCE.getRelativePath(file.getParentFile(), targetFile);
                                AttributesImpl newAtts = new AttributesImpl(atts);
                                int index = atts.getIndex(current.locationAttribute.getNamespaceURI(), current.locationAttribute.getLocalPart());
                                newAtts.setValue(index, href);
                                atts = newAtts;
                            }
                        }
                    }catch(IOException ex){
                        throw new SAXException(ex);
                    }
                }
            }else
                depth++;

            super.startElement(namespaceURI, localName, qualifiedName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException{
            super.endElement(uri, localName, qName);
            if(depth==0)
                current = current.parent;
            else
                depth--;
        }
    };

    /*-------------------------------------------------[ Resource Resolver ]---------------------------------------------------*/

    private Resolver resolver;
    public void setResolver(Resolver resolver){
        this.resolver = resolver;
    }

    private String resolveLink(String namespace, String location){
        String resolvedLocation = null;
        if(resolver!=null)
            resolvedLocation = resolver.resolve(namespace, url.toExternalForm(), location);
        if(resolvedLocation==null)
            resolvedLocation = URLUtil.toURI(url).resolve(location).toString();
        return resolvedLocation;
    }

    public static interface Resolver{
        public String resolve(String namespace, String base, String location);
    }

    /*-------------------------------------------------[ Crawling ]---------------------------------------------------*/

    private CrawlerListener listener;

    private URL url;
    private File file;
    private DelegatingOutputStream out;

    private Map<URL, File> crawled;
    private void setFile(File file) throws IOException{
        this.file = file;
        out.setDelegate(new FileOutputStream(file));
        crawled.put(url, file);
    }

    public File crawl(InputSource document, CrawlerListener listener, File file) throws IOException{
        if(document.getSystemId()==null)
            throw new IllegalArgumentException("InputSource without systemID can't be crawled");
        this.listener = listener;
        url = URLUtil.toURL(document.getSystemId());

        try{
            xmlFilter.setParent(SAXUtil.newSAXParser(true, false, false).getXMLReader());
            SAXSource source = new SAXSource(xmlFilter, document);
            out = new DelegatingOutputStream();
            if(file!=null)
                setFile(file);

            TransformerUtil.newTransformer(null, false, 4, null)
                    .transform(source, new StreamResult(out));
        }catch(SAXException ex){
            throw new IOException(ex);
        } catch(ParserConfigurationException ex){
            throw new IOException(ex);
        } catch(TransformerException ex){
            throw new IOException(ex);
        }
        return this.file;
    }

    public File crawlInto(InputSource document, File dir) throws IOException{
        return crawl(document, new DefaultCrawlerListener(dir), null);
    }

    public void crawl(InputSource document, File file) throws IOException{
        crawl(document, new DefaultCrawlerListener(file.getParentFile()), file);
    }

    private static class DelegatingOutputStream extends FilterOutputStream{
        public DelegatingOutputStream(){
            super(new ByteArrayOutputStream2());
        }

        public void setDelegate(OutputStream out) throws IOException{
            ByteSequence seq = ((ByteArrayOutputStream2)this.out).toByteSequence();
            out.write(seq.buffer(), seq.offset(), seq.length());
            this.out = out;
        }
    }

    public static void main(String[] args) throws Exception{
        if(args.length!=2){
            System.out.println("usage: crawl-xml."+(OS.get().isWindows()?"bat":"sh")+" <url> <dir>");
            System.exit(1);
        }

        File dir = new File(args[1]);
        FileUtil.mkdirs(dir);
        new XMLCrawler().crawlInto(new InputSource(args[0]), dir);
    }
}
/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.crawl;

import jlibs.core.io.FileNavigator;
import jlibs.core.io.FileUtil;
import jlibs.core.net.URLUtil;
import jlibs.xml.sax.helpers.NamespaceSupportReader;
import jlibs.xml.xsl.TransformerUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author Santhosh Kumar T
 */
public class XMLCrawler extends NamespaceSupportReader{
    private Stack<QName> path = new Stack<QName>();

    public XMLCrawler() throws ParserConfigurationException, SAXException{
        super(false);
    }

    private List<AttributeLink> links = new ArrayList<AttributeLink>();
    public void addLink(AttributeLink link){
        links.add(link);
    }

    private CharArrayWriter contents = new CharArrayWriter();

    @Override
    public void startDocument() throws SAXException{
        path.clear();
        super.startDocument();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException{
        contents.reset();
        path.push(new QName(namespaceURI, localName));

        for(AttributeLink link: links){
            try{
                if(link.matches(path)){
                    String location = link.resolve(atts);
                    if(location!=null){
                        URL linkURL = new URL(sourceURL, location);
                        location = URLUtil.resolve(sourceURL, linkURL).toString();
                        File linkFile = crawled.get(linkURL);
                        if(linkFile==null){
                            linkFile = link.suggestFile(sourceFile, location);
                            if(linkFile!=null)
                                pending.put(new InputSource(linkURL.toString()), linkFile);
                        }

                        AttributesImpl newAtts = new AttributesImpl(atts);
                        String newLocation;
                        if(linkFile!=null)
                            newLocation = FileNavigator.INSTANCE.getRelativePath(sourceFile.getParentFile(), linkFile);
                        else
                            newLocation = linkURL.toString();
                        link.repair(newAtts, newLocation);
                        atts = newAtts;

                        break;
                    }
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        
        super.startElement(namespaceURI, localName, qualifiedName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        super.endElement(uri, localName, qName);
        path.pop();
    }

    /*-------------------------------------------------[ Crawling ]---------------------------------------------------*/

    private File sourceFile;
    private URL sourceURL;
    private Map<URL, File> crawled = new LinkedHashMap<URL, File>();
    private Map<InputSource, File> pending = new LinkedHashMap<InputSource, File>();
    
    public File crawlInto(InputSource document, File dir, String... extensions) throws TransformerException, IOException{
        URL url = URLUtil.toURL(document.getSystemId());
        String fileName = URLUtil.suggestFile(URLUtil.toURI(url), extensions);
        File file = FileUtil.findFreeFile(new File(dir, fileName));
        crawl(document, file);
        return file;
    }

    public void crawl(InputSource document, File file) throws TransformerException, IOException{
        if(document.getSystemId()==null)
            throw new IllegalArgumentException("InputSource without systemID can't be crawled");
        
        sourceFile = file;
        sourceURL = URLUtil.toURL(document.getSystemId());
        if(crawled.containsKey(sourceURL))
            return;

        pending.clear();
        
        FileUtil.mkdirs(sourceFile.getParentFile());
        SAXSource source = new SAXSource(this, document);
        TransformerUtil.newTransformer(null, true, 4, null)
                .transform(source, new StreamResult(file));

        crawled.put(sourceURL, sourceFile);

        Map<InputSource, File> map = new LinkedHashMap<InputSource, File>(pending);
        for(Map.Entry<InputSource, File> entry: map.entrySet()){
            try{
                crawl(entry.getKey(), entry.getValue());
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
}

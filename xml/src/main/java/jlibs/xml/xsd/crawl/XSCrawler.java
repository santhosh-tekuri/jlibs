/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.xsd.crawl;

import jlibs.core.io.FileUtil;
import jlibs.xml.sax.crawl.XMLCrawler;
import org.xml.sax.InputSource;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class XSCrawler extends XMLCrawler{
    public XSCrawler(){
        addLink(new XSImport());
        addLink(new XSInclude());
    }

    public File crawlInto(InputSource document, File dir) throws TransformerException, IOException{
        return crawlInto(document, dir, "xsd");
    }

    public static void main(String[] args) throws Exception{
        String dir = "xml/xsds/crawl";
        String xsd = "xml/xsds/a.xsd";
        InputSource document = new InputSource(xsd);
        FileUtil.delete(new File(dir));
        new XSCrawler().crawlInto(document, new File(dir));
    }
}

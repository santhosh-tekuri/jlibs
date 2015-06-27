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

package jlibs.xml.sax.crawl;

import jlibs.core.io.FileUtil;
import jlibs.core.net.URLUtil;

import java.io.File;
import java.net.URL;

/**
 * @author Santhosh Kumar T
 */
public class DefaultCrawlerListener implements CrawlerListener{
    protected File dir;
    public DefaultCrawlerListener(File dir){
        this.dir = dir;
    }

    @Override
    public boolean doCrawl(URL url){
        return true;
    }

    @Override
    public File toFile(URL url, String extension){
        String fileName = URLUtil.suggestFile(URLUtil.toURI(url), extension);
        return FileUtil.findFreeFile(new File(dir, fileName));
    }
}

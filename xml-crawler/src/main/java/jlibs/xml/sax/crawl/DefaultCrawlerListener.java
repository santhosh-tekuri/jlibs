/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

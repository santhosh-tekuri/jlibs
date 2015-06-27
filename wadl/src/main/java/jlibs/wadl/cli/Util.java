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

package jlibs.wadl.cli;

import jlibs.core.io.FileUtil;
import jlibs.core.lang.ArrayUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar T
 */
public class Util{
    public static boolean isContentType(String contentType, String match){
        if(contentType==null)
            return false;
        int semicolon = contentType.indexOf(';');
        if(semicolon!=-1)
            contentType = contentType.substring(0, semicolon);
        if(("text/"+match).equalsIgnoreCase(contentType))
            return true;
        else if(contentType.startsWith("application/"))
            return contentType.endsWith("application/"+match) || contentType.endsWith("+"+match);
        else
            return false;
    }
    
    public static boolean isXML(String contentType){
        return isContentType(contentType, "xml");
    }
    
    public static boolean isJSON(String contentType){
        return isContentType(contentType, "json");
    }

    public static boolean isPlain(String contentType){
        return isContentType(contentType, "plain");
    }

    public static boolean isHTML(String contentType){
        return isContentType(contentType, "html");
    }

    public static String getExtension(String contentType){
        if(contentType==null)
            return "";
        int semicolon = contentType.indexOf(';');
        if(semicolon!=-1)
            contentType = contentType.substring(0, semicolon);
        if(ArrayUtil.contains(new String[]{ "application/x-zip-compressed", "application/zip" }, contentType))
            return "zip";
        else
            return "";
    }
    
    public static Map<String, List<String>> toMap(List<String> list, char separator){
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for(String item: list){
            int index = item.indexOf(separator);
            if(index!=-1){
                String key = item.substring(0, index);
                String value = item.substring(index+1);
                List<String> values = map.get(key);
                if(values==null)
                    map.put(key, values=new ArrayList<String>());
                values.add(value);
            }
        }
        return map;
    }

    public static File toFile(String str){
        return new File(str.replace("~", FileUtil.USER_HOME.getAbsolutePath()));
    }
}

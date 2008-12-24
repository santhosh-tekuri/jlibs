package jlibs.core.net;

import jlibs.core.io.FileUtil;
import jlibs.core.lang.StringUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public class URLUtil{
    public static URL toURL(String systemID){
        if(StringUtil.isWhitespace(systemID))
            return null;
        systemID = systemID.trim();
        try{
            return new URL(systemID);
        }catch(MalformedURLException ex){
            return FileUtil.toURL(new File(systemID));
        }
    }

    public static Map<String, String> getQueryParams(String uri, String encoding) throws URISyntaxException, UnsupportedEncodingException{
        if(encoding==null)
            encoding = "UTF-8";
        
        String query = new URI(uri).getRawQuery();
        String params[] = Pattern.compile("&", Pattern.LITERAL).split(query);
        Map<String, String> map = new HashMap<String, String>(params.length);
        for(String param: params){
            int equal = param.indexOf('=');
            String name = param.substring(0, equal);
            String value = param.substring(equal+1);
            name = URLDecoder.decode(name, encoding);
            value = URLDecoder.decode(value, encoding);
            map.put(name, value);
        }
        return map;
    }

    public static void main(String[] args) throws Exception{
        System.out.println(getQueryParams("http://www.google.co.in/search?hl=en&client=firefox-a&rls=org.mozilla%3Aen-US%3Aofficial&hs=Jvw&q=java%26url+encode&btnG=Search&meta=&aq=f&oq=", null));
    }
}

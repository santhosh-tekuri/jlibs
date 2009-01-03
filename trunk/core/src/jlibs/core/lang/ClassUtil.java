package jlibs.core.lang;

import jlibs.core.io.FileUtil;
import jlibs.core.net.URLUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Santhosh Kumar T
 */
public class ClassUtil{
    public static String getClassPath(Class clazz){
        URL url = clazz.getResource(clazz.getSimpleName() + ".class");
        if("jar".equals(url.getProtocol())){
            String path = url.getPath();
            try{
                return URLUtil.toSystemID(new URL(path.substring(0, path.lastIndexOf('!'))));
            }catch(MalformedURLException ex){
                throw new ImpossibleException("as per JAR URL Syntax this should never happen", ex);
            }
        }
        String resource = URLUtil.toSystemID(url);
        String relativePath = "/"+clazz.getName().replace(".", FileUtil.SEPARATOR)+".class";
        if(resource.endsWith(relativePath))
            resource = resource.substring(0, resource.length()-relativePath.length());
        return resource;
    }

    public static void main(String[] args){
        System.out.println(getClassPath(String.class));
        System.out.println(getClassPath(ClassUtil.class));
    }
}

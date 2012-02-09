package jlibs.wadl.cli;

/**
 * @author Santhosh Kumar T
 */
public class Util{
    public static boolean isXML(String contentType) {
        if(contentType==null)
            return false;
        int semicolon = contentType.indexOf(';');
        if(semicolon!=-1)
            contentType = contentType.substring(0, semicolon);
        if("text/xml".equalsIgnoreCase(contentType))
            return true;
        else if(contentType.startsWith("application/"))
            return contentType.endsWith("application/xml") || contentType.endsWith("+xml");
        else
            return false;
    }
}

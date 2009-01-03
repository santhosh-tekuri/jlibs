package jlibs.xml;

import jlibs.core.lang.StringUtil;

import java.util.Stack;
import java.util.StringTokenizer;

/**
 * @author Santhosh Kumar T
 */
public class ClarkName{
    public static String valueOf(String namespace, String localPart){
        return StringUtil.isEmpty(namespace) ? localPart : '{'+namespace+'}'+localPart;
    }

    public static String[] split(String clarkName){
        int end = clarkName.lastIndexOf('}');
        if(end==-1)
            return new String[]{ "", clarkName };
        else
            return new String[]{ clarkName.substring(1, end), clarkName.substring(end+1) };
    }

    public static String[] splitPath(String clarkPath){
        Stack<String> tokens = new Stack<String>();
        boolean foundNamespace = false;
        StringTokenizer stok = new StringTokenizer(clarkPath, "/", true);
        while(stok.hasMoreTokens()){
            String token = stok.nextToken();
            if(foundNamespace)
                token = tokens.pop() + token;

            if(token.charAt(0)=='{')
                foundNamespace = true;
            if(token.indexOf('}')!=-1)
                foundNamespace = false;
            if(foundNamespace || !token.equals("/"))
                tokens.push(token);
        }
        return tokens.toArray(new String[tokens.size()]);
    }
}

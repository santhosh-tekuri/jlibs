package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public class StringUtil{
    public static boolean isEmpty(CharSequence str){
        return str==null || str.length()==0;
    }

    public static boolean isWhitespace(CharSequence str){
        if(str!=null){
            for(int i=0; i<str.length(); i++){
                if(!Character.isWhitespace(str.charAt(i)))
                    return false;
            }
        }
        return true;
    }
}

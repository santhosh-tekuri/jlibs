package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public class StringUtil{
    /**
     * returns true if <code>str</code> is null or
     * its length is zero
     */
    public static boolean isEmpty(CharSequence str){
        return str==null || str.length()==0;
    }

    /**
     * returns true if <code>str</code> is null or
     * it contains only whitespaces.
     * <p>
     * {@link Character#isWhitespace(char)} is used
     * to test for whitespace
     */
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

package jlibs.core.lang;

import java.util.StringTokenizer;

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

    public static String[] getTokens(String str, String delim, boolean trim){
        StringTokenizer stok = new StringTokenizer(str, delim);
        String tokens[] = new String[stok.countTokens()];
        for(int i=0; i<tokens.length; i++){
            tokens[i] = stok.nextToken();
            if(trim)
                tokens[i] = tokens[i].trim();
        }
        return tokens;
    }
}

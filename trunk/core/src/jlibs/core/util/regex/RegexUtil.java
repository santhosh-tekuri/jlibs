package jlibs.core.util.regex;

import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public class RegexUtil{
    public static Pattern compilePathPattern(String pathPattern){
        return Pattern.compile(pathPattern.replace("**", ".+?").replace("*", "[^/]+?"));
    }
}

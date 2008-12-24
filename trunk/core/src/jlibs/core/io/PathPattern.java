package jlibs.core.io;

import jlibs.core.graph.Filter;

import java.util.regex.Pattern;

/**
 * @author Santhosh Kumar T
 */
public class PathPattern{
    private class Include implements Filter{
        private Pattern pattern;
        private Include(String pathPattern){
            this.pattern = Pattern.compile(pathPattern.replace("**", ".+").replace("*", "[^/]+"));
        }
        
        @Override
        public boolean select(Object elem){
            return false;
        }
    }
}

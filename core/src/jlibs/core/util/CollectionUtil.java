package jlibs.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Santhosh Kumar T
 */
public class CollectionUtil{
    public static Properties readProperties(InputStream is, Properties defaults) throws IOException{
        Properties props = new Properties(defaults);
        try{
            props.load(is);
        }finally{
            is.close();
        }
        return props;
    }
}

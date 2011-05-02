package jlibs.core.nio.channels;

import java.io.IOException;

/**
 * @author Santhosh Kumar T
 */
public class ChunkException extends IOException{
    public ChunkException(String message){
        super(message);
    }
}

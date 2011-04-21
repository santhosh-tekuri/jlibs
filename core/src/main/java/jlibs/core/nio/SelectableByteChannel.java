package jlibs.core.nio;

import java.io.IOException;
import java.nio.channels.ByteChannel;

/**
 * @author Santhosh Kumar T
 */
public interface SelectableByteChannel extends ByteChannel{
    long id();
    public void addInterest(int operation) throws IOException;
    public void removeInterest(int operation) throws IOException;

    public void attach(Object obj);
    public Object attachment();
}

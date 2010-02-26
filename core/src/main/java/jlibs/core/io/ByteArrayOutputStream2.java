package jlibs.core.io;

import jlibs.core.ByteSequence;

import java.io.ByteArrayOutputStream;

/**
 * @author Santhosh Kumar T
 */
public class ByteArrayOutputStream2 extends ByteArrayOutputStream{
    public ByteArrayOutputStream2(){
    }

    public ByteArrayOutputStream2(int size){
        super(size);
    }

    public ByteSequence toByteSequence(){
        return new ByteSequence(buf, 0, size());
    }
}

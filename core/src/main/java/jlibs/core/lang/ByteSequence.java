package jlibs.core.lang;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Santhosh Kumar T
 */
public class ByteSequence{
    private byte buff[];
    private int offset;
    private int length;

    public ByteSequence(byte[] buff, int offset, int length){
        set(buff, offset, length);
    }

    public ByteSequence(byte[] buff){
        set(buff, 0, buff.length);
    }

    public void set(byte[] buff, int offset, int length){
        this.buff = buff;
        this.offset = offset;
        this.length = length;
    }

    public byte byteAt(int index){
        if(index<0 || index>=length)
            throw new IndexOutOfBoundsException(index+" is not in range [0, "+length+")");
        else
            return buff[offset+index];
    }

    public byte[] buffer(){
        return buff;
    }

    public int offset(){
        return offset;
    }
    
    public int length(){
        return length;
    }

    public byte[] toByteArray(boolean clone){
        if(!clone){
            if(offset==0 && buff.length==length)
                return buff;
        }
        byte array[] = new byte[length];
        System.arraycopy(buff, offset, array, 0, length);
        return array;
    }

    public ByteArrayInputStream asInputStream(){
        return new ByteArrayInputStream(buffer(), offset(), length());
    }

    public String toString(String charset) throws UnsupportedEncodingException{
        return new String(buff, offset, length, charset);
    }

    @Override
    public String toString(){
        return new String(buff, offset, length);
    }
}

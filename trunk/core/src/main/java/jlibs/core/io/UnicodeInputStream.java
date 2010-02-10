package jlibs.core.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Santhosh Kumar T
 */
public class UnicodeInputStream extends FilterInputStream{
    public final boolean hasBOM;
    public final BOM bom;

    public UnicodeInputStream(InputStream delegate) throws IOException{
        super(delegate);

        int len = IOUtil.readFully(delegate, marker);
        BOM bom = null;
        if(len==4){
            bom = BOM.get(marker, true);
            if(bom!=null)
                imarker = bom.with().length;
            else
                bom = BOM.get(marker, false);
        }
        this.bom = bom;
        hasBOM = imarker>0;
    }


    private byte marker[] = new byte[4];
    private int imarker;

    @Override
    public int read() throws IOException{
        if(marker!=null){
            int b = marker[imarker++];
            if(imarker==marker.length)
                marker = null;
            return b;
        }else
            return super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        int read = 0;
        while(marker!=null && len>0){
            b[off] = (byte)read();
            off++;
            len--;
            read++;
        }
        int r = super.read(b, off, len);
        if(read==0)
            return r;
        else
            return r==-1 ? read : read+r;
    }
}

package jlibs.core.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Santhosh Kumar T
 */
public interface EncodingDetector{
    public String detect(ByteBuffer buffer);

    public static final EncodingDetector DEFAULT = new EncodingDetector(){
        @Override
        public String detect(ByteBuffer buffer){
            BOM bom = BOM.detect(buffer);
            return bom==null ? null : bom.encoding();
        }
    };

    // see http://www.w3.org/TR/REC-xml/#sec-guessing-no-ext-info
    public static final EncodingDetector XML = new EncodingDetector(){
        final int[] UTF32_LE = { 0x3C, 0x00, 0x00, 0x00 };
        final int[] UTF32_BE = { 0x00, 0x00, 0x00, 0x3C };
        final int[] UTF_16LE = { 0x3C, 0x00, 0x3F, 0x00 };
        final int[] UTF_16BE = { 0x00, 0x3C, 0x00, 0x3F };
        final int[] UTF_8    = { 0x3C, 0x3F, 0x78, 0x6D };
        final int[] CP037    = { 0x4C, 0x6F, 0xA7, 0x94 };

        @Override
        public String detect(ByteBuffer buffer){
            String encoding = DEFAULT.detect(buffer);
            if(encoding!=null)
                return encoding;

            if(buffer.remaining()>=4){
                int values[] = { buffer.get()&0xFF, buffer.get()&0xFF, buffer.get()&0xFF, buffer.get()&0xFF };
                buffer.position(buffer.position()-4);
                if(Arrays.equals(UTF32_LE, values))
                    return IOUtil.UTF_32LE.name();
                else if(Arrays.equals(UTF32_BE, values))
                    return IOUtil.UTF_32BE.name();
                else if(Arrays.equals(UTF_16LE, values))
                    return IOUtil.UTF_16LE.name();
                else if(Arrays.equals(UTF_16BE, values))
                    return IOUtil.UTF_16BE.name();
                else if(Arrays.equals(UTF_8, values))
                    return IOUtil.UTF_8.name();
                else if(Arrays.equals(CP037, values))
                    return "Cp037";

            }
            return IOUtil.UTF_8.name();
        }
    };

    // see http://www.ietf.org/rfc/rfc4627 section 3
    // 00 00 00 xx  UTF-32BE
    // 00 xx 00 xx  UTF-16BE
    // xx 00 00 00  UTF-32LE
    // xx 00 xx 00  UTF-16LE
    // xx xx xx xx  UTF-8
    public static final EncodingDetector JSON = new EncodingDetector(){
        @Override
        public String detect(ByteBuffer buffer){
            String encoding = DEFAULT.detect(buffer);
            if(encoding!=null)
                return encoding;

            if(buffer.remaining()>=4){
                int flag = 0;
                for(int i=0; i<4; i++){
                    if(buffer.get()!=0x00)
                        flag |= 1 << i;
                }
                buffer.position(buffer.position()-4);

                switch(flag){
                    case 1:
                        return IOUtil.UTF_32LE.name();
                    case 5:
                        return IOUtil.UTF_16LE.name();
                    case 8:
                        return IOUtil.UTF_32BE.name();
                    case 10:
                        return IOUtil.UTF_16BE.name();
                }
            }
            return IOUtil.UTF_8.name();
        }
    };
}

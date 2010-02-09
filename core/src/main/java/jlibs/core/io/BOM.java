package jlibs.core.io;

/**
 * @author Santhosh Kumar T
 */
public enum BOM{
    UTF8(       new int[]{0xEF, 0xBB, 0xBF}      , null),
    UCS4_LE(    new int[]{0xFF, 0xFE, 0x00, 0x00}, new int[]{0x3C, 0x00, 0x00, 0x00}),
    UCS4_BE(    new int[]{0x00, 0x00, 0xFE, 0xFF}, new int[]{0x00, 0x00, 0x00, 0x3C}),
    UCS4_2143(  new int[]{0x00, 0x00, 0xFF, 0xFE}, new int[]{0x00, 0x00, 0x3C, 0x00}),
    UCS4_3412(  new int[]{0xFE, 0xFF, 0x00, 0x00}, new int[]{0x00, 0x3C, 0x00, 0x00}),

    UTF16_LE(   new int[]{0xFF, 0xFE}            , new int[]{0x3C, 0x00, 0x3F, 0x00}),
    UTF16_BE(   new int[]{0xFE, 0xFF}            , new int[]{0x00, 0x3C, 0x00, 0x3F}),
    ASCII(      null                             , new int[]{0x3C, 0x3F, 0x78, 0x6D}),
    EBCDIC(     null                             , new int[]{0x4C, 0x6F, 0xA7, 0x94}),
    ;

    byte with[];
    byte without[];

    BOM(int[] with, int[] without){
        this.with = toBytes(with);
        this.without = toBytes(without);
    }

    private static byte[] toBytes(int[] arr){
        byte b[] = new byte[arr.length];
        for(int i=0; i<arr.length; i++)
            b[i] = (byte)arr[i];
        return b;
    }

    public byte[] with(){
        return with;
    }

    public byte[] without(){
        return without;
    }

    public static BOM get(byte b[], boolean with){
        for(BOM bom : values()){
            byte expected[] = with ? bom.with() : bom.without();
            if(expected!=null && b.length>=expected.length){
                boolean matched = true;
                for(int i=0; i<expected.length; i++){
                    if(expected[i]!=b[i]){
                        matched = false;
                        break;
                    }
                }
                if(matched)
                    return bom;
            }
        }
        return null;
    }
}

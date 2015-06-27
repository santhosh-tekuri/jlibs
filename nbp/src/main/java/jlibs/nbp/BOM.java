/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.nbp;

/**
 * see http://www.w3.org/TR/REC-xml/#sec-guessing-no-ext-info
 *
 * @author Santhosh Kumar T
 */
public enum BOM{
    UTF8        ("UTF-8"               , new int[]{0xEF, 0xBB, 0xBF}      , null),
    UCS4_LE     ("UCS-4LE"             , new int[]{0xFF, 0xFE, 0x00, 0x00}, new int[]{0x3C, 0x00, 0x00, 0x00}),
    UCS4_BE     ("UCS-4BE"             , new int[]{0x00, 0x00, 0xFE, 0xFF}, new int[]{0x00, 0x00, 0x00, 0x3C}),
    UCS4_2143   ("UCS-4-2143"          , new int[]{0x00, 0x00, 0xFF, 0xFE}, new int[]{0x00, 0x00, 0x3C, 0x00}),
    UCS4_3412   ("UCS-4-3412"          , new int[]{0xFE, 0xFF, 0x00, 0x00}, new int[]{0x00, 0x3C, 0x00, 0x00}),

    UTF16_LE    ("UTF-16LE"            , new int[]{0xFF, 0xFE}            , new int[]{0x3C, 0x00, 0x3F, 0x00}),
    UTF16_BE    ("UTF-16BE"            , new int[]{0xFE, 0xFF}            , new int[]{0x00, 0x3C, 0x00, 0x3F}),
    ASCII       ("UTF-8"               , null                             , new int[]{0x3C, 0x3F, 0x78, 0x6D}),
    EBCDIC      ("Cp037"               , null                             , new int[]{0x4C, 0x6F, 0xA7, 0x94}),
    ;

    private String encoding;
    private byte with[];
    private byte without[];

    BOM(String encoding, int[] with, int[] without){
        this.encoding = encoding;
        this.with = toBytes(with);
        this.without = toBytes(without);
    }

    private static byte[] toBytes(int[] arr){
        if(arr==null)
            return null;
        else{
            byte b[] = new byte[arr.length];
            for(int i=0; i<arr.length; i++)
                b[i] = (byte)arr[i];
            return b;
        }
    }

    public String encoding(){
        return encoding;
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

/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

package jlibs.core.io;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 * 
 * @author Santhosh Kumar T
 */
public enum BOM{
    UTF32_LE("UTF-32LE"),
    UTF32_BE("UTF-32BE"),
    UTF16_LE("UTF-16LE"),
    UTF16_BE("UTF-16BE"),
        UTF8("UTF-8");

    private String encoding;
    private int expected[];

    BOM(String encoding){
        this.encoding = encoding;

        byte byteBom[] = "\uFEFF".getBytes(Charset.forName(encoding));
        expected = new int[byteBom.length];
        for(int i=0; i<byteBom.length; i++)
            expected[i] = byteBom[i]&0xFF;
    }

    public String encoding(){
        return encoding;
    }

    public int[] expected(){
        return expected;
    }

    private boolean matches(ByteBuffer buffer){
        int pos = buffer.position();
        for(int i: expected){
            if(i!=(buffer.get()&0xFF)){
                buffer.position(pos);
                return false;
            }
        }
        return true;
    }

    public static BOM detect(ByteBuffer buffer){
        int available = buffer.remaining();
        for(BOM bom: values()){
            if(available>=bom.expected.length){
                if(bom.matches(buffer))
                    return bom;
            }else
                return null;
        }
        return null;
    }
}

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

package jlibs.xml.sax.async;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public enum AttributeType{
    NMTOKEN,
    ENTITY,
    ID,
    IDREF,
    ENUMERATION,
    NMTOKENS,
    ENTITIES,
    IDREFS,
    NOTATION,
    CDATA;

    public String normalize(String value){
        switch(this){
            case NMTOKEN:
            case ENTITY:
            case ID:
            case IDREF:
            case ENUMERATION:
                return value.trim();
            case NMTOKENS:
            case ENTITIES:
            case IDREFS:
                return toNMTOKENS(value);
            default:
                return value;
        }
    }
    
    public String toString(List<String> validValues){
        switch(this){
            case NOTATION:
            case ENUMERATION:
                StringBuilder buff = new StringBuilder();
                if(this==NOTATION)
                    buff.append(name()).append(' ');
                buff.append('(');
                for(int i=0; i<validValues.size(); i++){
                    if(i>0)
                        buff.append('|');
                    buff.append(validValues.get(i));
                }
                buff.append(')');
                return buff.toString();
            default:
                return name();
        }
    }

    private static String toNMTOKENS(String value){
        char[] buffer = value.toCharArray();
        int write = 0;
        int lastWrite = 0;
        boolean wroteOne = false;

        int read = 0;
        while(read<buffer.length && buffer[read]==' '){
            read++;
        }

        int len = buffer.length;
        while(len<read && buffer[read]==' ')
            len--;

        while(read<len){
            if(buffer[read]==' '){
                if(wroteOne)
                    buffer[write++] = ' ';

                do{
                    read++;
                }while(read<len && buffer[read]==' ');
            }else{
                buffer[write++] = buffer[read++];
                wroteOne = true;
                lastWrite = write;
            }
        }

        value = new String(buffer, 0, lastWrite);
        return value;
    }

    public static String toPublicID(String value){
        char[] buffer = value.toCharArray();
        int write = 0;
        int lastWrite = 0;
        boolean wroteOne = false;

        int read = 0;
        while(read<buffer.length && Character.isWhitespace(buffer[read])){
            read++;
        }

        int len = buffer.length;
        while(len<read && Character.isWhitespace(buffer[read]))
            len--;

        while(read<len){
            if(Character.isWhitespace(buffer[read])){
                if(wroteOne)
                    buffer[write++] = ' ';

                do{
                    read++;
                }while(read<len && Character.isWhitespace(buffer[read]));
            }else{
                buffer[write++] = buffer[read++];
                wroteOne = true;
                lastWrite = write;
            }
        }

        value = new String(buffer, 0, lastWrite);
        return value;
    }
}

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

package jlibs.xml.sax.async;

/**
 * @author Santhosh Kumar T
 */
final class QName{
    final String prefix;
    final String localName;
    final String name;
    final char chars[];
    final int hash;

    QName next;

    QName(int prefixLength, char[] buffer, int offset, int length, int hash, QName next){
        name = new String(buffer, offset, length);
        if(prefixLength==0){
            prefix = "";
            localName = name;
        }else{
            prefix = name.substring(0, prefixLength);
            localName = name.substring(prefixLength+1);
        }
        System.arraycopy(buffer, offset, chars=new char[length], 0, length);
        this.hash = hash;
        this.next = next;
    }
}

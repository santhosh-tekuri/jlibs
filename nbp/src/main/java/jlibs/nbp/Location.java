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
 * @author Santhosh Kumar T
 */
public class Location{
    private int line, col, offset;
    public Location(){
        reset();
    }

    public int getLineNumber(){ return line; }
    public int getColumnNumber(){ return col; }
    public int getCharacterOffset(){ return offset; }
    public void set(Location that){
        this.line = that.line;
        this.col = that.col;
        this.offset = that.offset;
        this.skipLF = that.skipLF;
    }

    private boolean skipLF;

    /**
     * return value tells whether the given character
     * has been included in location or not
     *
     * for example in sequence "\r\n", the character
     * '\n' is not included in location.
     */
    public boolean consume(int ch){
        offset++;
        if(ch==0x0D){
            skipLF = true;
            line++;
            col = 0;
            return true;
        }else if(ch==0x0A){
            if(skipLF){
                skipLF = false;
                return false;
            }else{
                line++;
                col = 0;
                return true;
            }
        }else{
            skipLF = false;
            col++;
            return true;
        }
    }

    public void reset(){
        line = col = offset = 0;
        skipLF = false;
    }

    @Override
    public String toString(){
        return line+":"+col;
    }
}

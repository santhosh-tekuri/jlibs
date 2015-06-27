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

import java.io.IOException;
import java.util.Arrays;

import static java.lang.Character.*;

/**
 * @author Santhosh Kumar T
 */
public abstract class NBParser{
    public static final boolean SHOW_STATS = false;
    public static int callRuleCount = 0;
    public static int chunkCount = 0;
    public void printStats(){
        System.out.println("callRuleCount = " + callRuleCount);
        System.out.println("chunkCount = " + chunkCount);
        System.out.println("lineCount = " + getLineNumber());
        System.out.println("charCount = " + getCharacterOffset());
    }

    protected final Chars buffer = new Chars();

    private int startingRule;
    public NBParser(int maxLookAhead, int startingRule){
        la = new int[maxLookAhead];
        reset(startingRule);
    }

    public final void reset(int rule){
        laLen = 0;
        stop = pop = false;
        marker = EOC;
        start = position = limit = 0;
        offset = linePosition = 0;
        line = 1;
        lastChar = 'X';
        buffer.clear();

        free = 2;
        stack[0] = startingRule = rule;
        stack[1] = 0;
    }

    public final void reset(){
        reset(startingRule);
    }

    protected char input[];
    protected int start;
    protected int position;
    protected int limit;
    protected int marker;

    public static final int EOF = -1;
    public static final int EOC = -2;
    protected int increment;
    protected final int codePoint() throws IOException{
        if(position==limit)
            return marker;

        int ch0 = input[position];
        if(ch0>=MIN_HIGH_SURROGATE && ch0<=MAX_HIGH_SURROGATE){
            if(position+1==limit)
                return EOC;

            int ch1 = input[position+1];
            if(ch1>=MIN_LOW_SURROGATE && ch1<=MAX_LOW_SURROGATE){
                increment = 2;
                return ((ch0 - MIN_HIGH_SURROGATE) << 10) + (ch1 - MIN_LOW_SURROGATE) + MIN_SUPPLEMENTARY_CODE_POINT;
            }else
                throw ioError("bad surrogate pair");
        }else{
            increment = 1;
            return ch0;
        }
    }

    public boolean coalesceNewLines = false;
    protected static final int FROM_LA = -2;
    protected final void consume(int cp){
        if(cp==FROM_LA){
            laPosition += laIncrement;
            cp = la[0];
        }else
            position += increment;

        assert cp!=EOF;
        if(cp=='\r'){
            line++;
            linePosition = position;
            cp = coalesceNewLines ? '\n' : '\r';
        }else if(cp=='\n'){
            linePosition = position;
            char lastChar = position==start+1 ? this.lastChar : input[position-2];
            if(lastChar!='\r')
                line++;
            else if(coalesceNewLines)
                    return;
        }
        if(buffer.free>0)
            buffer.append(cp);
    }

    protected int la[];
    protected int laLen;
    private int laPosition;
    private int laIncrement;
    protected final void addToLookAhead(int cp){
        if(laLen==0){
            laPosition = position;
            laIncrement = increment;
        }
        la[laLen++] = cp;
        position += increment;
    }

    protected final void resetLookAhead(){
        this.position = laPosition;
        laLen = 0;
    }

    protected int offset, line, linePosition;
    protected char lastChar;

    public final int getCharacterOffset(){
        return offset + (position-start);
    }

    public final int getLineNumber(){
        return line;
    }

    public final int getColumnNumber(){
        return position-linePosition;
    }

    public final void setLocation(NBParser parser){
        this.offset = parser.offset;
        this.line = parser.line;
        this.linePosition = parser.linePosition;
        this.lastChar = parser.lastChar;
    }

    public boolean stop, pop;
    public final int consume(char chars[], int position, int limit, boolean eof) throws IOException{
        if(SHOW_STATS){
            chunkCount++;
            if(chars!=null)
                System.out.println("chunk["+chunkCount+"] = {"+new String(chars, position, limit-position)+'}');
        }
        try{
            input = chars;
            start = this.position = position;
            this.limit = limit;
            marker = eof ? EOF : EOC;

            stop = pop = false;

            do{
                if(free==0){
                    if(this.position<limit)
                        throw expected(codePoint(), "<EOF>");
                    break;
                }
                free -= 2;
            }while(callRule(stack[free], stack[free+1]));

            if(laLen>0)
                resetLookAhead();
            if(exitFree>0){
                if(free+exitFree>stack.length)
                    stack = Arrays.copyOf(stack, Math.max(free+exitFree, free*2));
                do{
                    free += 2;
                    stack[free-2] = exitStack[exitFree-2];
                    stack[free-1] = exitStack[exitFree-1];
                    exitFree -= 2;
                }while(exitFree!=0);
            }else if(free==0 && this.position==limit && eof)
                onSuccessful();

            if(this.position!=position){
                lastChar = input[this.position-1];
                offset += this.position-position;
                linePosition -= this.position;
                position = this.position;
            }
            start = this.position = 0;

            return position;
        }catch(IOException ex){
            throw ex;
        }catch(Exception ex){
            if(ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            else
                throw new IOException(ex);
        }
    }

    protected abstract boolean callRule(int rule, int state) throws Exception;

    protected final Exception expected(int ch, String... matchers){
        String found;
        if(laLen>0)
            found = la[laLen-1]==EOF ? new String(la, 0, laLen-1).concat("<EOF>") : new String(la, 0, laLen);
        else{
            if(ch==EOF)
                found = "<EOF>";
            else
                found = new String(toChars(ch));
        }
        StringBuilder buff = new StringBuilder();
        for(String matcher: matchers){
            if(buff.length()>0)
                buff.append(" OR ");
            buff.append(matcher);
        }

        return fatalError("Found: '"+found+"' Expected: "+buff.toString());
    }

    protected abstract Exception fatalError(String message);
    protected abstract void onSuccessful() throws Exception;

    /*-------------------------------------------------[ Parsing Status ]---------------------------------------------------*/

    protected int free = 0;
    protected int stack[] = new int[100];

    public final IOException ioError(String message) throws IOException{
        Exception ex = fatalError(message);
        return ex instanceof IOException ? (IOException)ex : new IOException(ex);
    }

    protected int exitStack[] = new int[100];
    protected int exitFree = 0;
    protected final void exiting(int rule, int state){
        exitFree += 2;
        if(exitFree>exitStack.length)
            exitStack = Arrays.copyOf(exitStack, exitFree*2);
        exitStack[exitFree-2] = rule;
        exitStack[exitFree-1] = state;
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    public static final int RULE_DYNAMIC_STRING_MATCH = Integer.MIN_VALUE;
    public char[] dynamicStringToBeMatched;

    // NOTE: use only when bufferring is off and no new lines in expected
    protected final boolean matchString(int state, char expected[]) throws Exception{
        int length = expected.length;

        while(state<length && position<limit){
            if(input[position]!=expected[state])
                throw expected(codePoint(), new String(new int[]{ Character.codePointAt(expected, state) }, 0, 1));
            state++;
            position++;
        }
        if(state==length)
            return true;
        else{
            if(marker==EOF)
                throw expected(EOF, new String(expected, state, length-state));
            exiting(RULE_DYNAMIC_STRING_MATCH, state);
            return false;
        }
    }

    protected final boolean matchString(int rule, int state, int expected[]) throws Exception{
        int length = expected.length;

        for(int i=state; i<length; i++){
            int cp = codePoint();
            if(cp!=expected[i]){
                if(cp==EOC){
                    exiting(rule, i);
                    return false;
                }
                throw expected(cp, new String(expected, i, 1));
            }
            consume(cp);
        }
        return true;
    }

    protected final int finishAll(int ch, int expected) throws IOException{
        while(ch==expected){
            consume(ch);
            ch = codePoint();
        }
        return ch;
    }

    protected final int finishAll_OtherThan(int ch, int expected) throws IOException{
        while(ch>=0 && ch!=expected){
            consume(ch);
            ch = codePoint();
        }
        return ch;
    }
}
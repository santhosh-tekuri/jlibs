/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
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

    protected final Buffer buffer = new Buffer();

    private int startingRule;
    public NBParser(int maxLookAhead, int startingRule){
        la = new int[maxLookAhead];
        reset(startingRule);
    }

    public final void reset(int rule){
        laLen = 0;
        stop = false;
        eof = eofSent = false;
        start = position = limit = 0;
        offset = lineOffset = 0;
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

    private char input[];
    private int start;
    private int position;
    private int limit;
    private boolean eof;
    private boolean eofSent;

    protected final int EOF = -1;
    protected final int EOC = -2;
    private int increment;
    protected final int codePoint() throws IOException{
        if(position==limit){
            if(eof){
                eofSent = true;
                return EOF;
            }else
                return EOC;
        }

        char ch0 = input[position];
        if(ch0>=MIN_HIGH_SURROGATE && ch0<=MAX_HIGH_SURROGATE){
            if(position+1==limit)
                return EOC;

            char ch1 = input[position+1];
            if(ch1>=MIN_LOW_SURROGATE && ch1<=MAX_LOW_SURROGATE){
                increment = 2;
                return ((ch0 - MIN_HIGH_SURROGATE) << 10) + (ch1 - MIN_LOW_SURROGATE) + MIN_SUPPLEMENTARY_CODE_POINT;
            }else{
                ioError("bad surrogate pair");
                throw new Error("Impossible");
            }
        }else{
            increment = 1;
            return ch0;
        }
    }

    public boolean coelsceNewLines = false;
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
            lineOffset = getCharacterOffset();
            cp = coelsceNewLines ? '\n' : '\r';
        }else if(cp=='\n'){
            lineOffset = getCharacterOffset();
            char lastChar = position==start+1 ? this.lastChar : input[position-2];
            if(lastChar!='\r')
                line++;
            else if(coelsceNewLines)
                    return;
        }
        if(buffer.isBufferring())
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

    private int offset, line, lineOffset;
    private char lastChar;

    public final int getCharacterOffset(){
        return offset + (position-start);
    }

    public final int getLineNumber(){
        return line;
    }

    public final int getColumnNumber(){
        return getCharacterOffset()-lineOffset;
    }

    public final void setLocation(NBParser parser){
        this.offset = parser.offset;
        this.line = parser.line;
        this.lineOffset = parser.lineOffset;
        this.lastChar = parser.lastChar;
    }

    public boolean stop;
    public final int consume(char chars[], int position, int limit, boolean eof) throws IOException{
        if(SHOW_STATS){
            chunkCount++;
            if(chars!=null)
                System.out.println("chunk["+chunkCount+"] = {"+new String(chars, position, limit-position)+'}');
        }
        try{
            stop = false;
            input = chars;
            start = this.position = position;
            this.limit = limit;
            this.eof = eof;

            int rule, state;
            do{
                if(free==0){
                    int cp = codePoint();
                    if(cp>=0)
                        expected(cp, "<EOF>");
                    break;
                }
                rule = stack[free-2];
                state = stack[free-1];
                free -= 2;
            }while(callRule(rule, state));

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
            }

            if(eofSent)
                onSuccessful();
            if(this.position!=position)
                lastChar = input[this.position-1];

            offset += this.position-position;
            position = this.position;
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

    protected final void expected(int ch, String... matchers) throws Exception{
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

        String message = "Found: '"+found+"' Expected: "+buff.toString();
        fatalError(message);
        throw new IOException(message);
    }

    protected abstract void fatalError(String message) throws Exception;
    protected abstract void onSuccessful() throws Exception;

    /*-------------------------------------------------[ Parsing Status ]---------------------------------------------------*/

    protected int free = 0;
    protected int stack[] = new int[100];

    protected final void ioError(String message) throws IOException{
        try{
            fatalError(message);
            throw new IOException(message);
        }catch(IOException ex){
            throw ex;
        }catch(Exception ex){
            throw new IOException(ex);
        }
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
                expected(codePoint(), new String(new int[]{ Character.codePointAt(expected, state) }, 0, 1));
            state++;
            position++;
        }
        if(state==length)
            return true;
        else{
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
                expected(cp, new String(expected, i, 1));
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
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
        System.out.println("lineCount = " + (location.getLineNumber()+1));
        System.out.println("charCount = " + location.getCharacterOffset());
    }

    private final Stream stream;
    protected final Stream.LookAhead lookAhead;
    public final Location location = new Location();
    protected final Buffer buffer = new Buffer();

    private int startingRule;
    public NBParser(int maxLookAhead, int startingRule){
        stream = new Stream(maxLookAhead);
        lookAhead = stream.lookAhead;
        reset(startingRule);
    }

    public final void reset(int rule){
        stream.clear();
        location.reset();
        buffer.clear();

        free = 2;
        stack[0] = startingRule = rule;
        stack[1] = 0;
    }

    public final void reset(){
        reset(startingRule);
    }

    private char input[];
    private int position;
    private int limit;

    protected final int EOF = -1;
    protected final int EOC = -2;
    private int increment;
    protected final int codePoint() throws IOException{
        int cp = lookAhead.getNext();
        if(cp!=EOC)
            return cp;
        if(position==limit){
            assert input!=null;
            return EOC;
        }

        if(input==null){
            increment = 1;
            return EOF;
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
        if(stream.length()>0){
            if(cp==FROM_LA)
                cp = stream.charAt(0);
            lookAhead.consumed();
        }else
            position += increment;

        assert cp!=EOF;
        if(coelsceNewLines){
            if(location.consume(cp) && buffer.isBufferring())
                buffer.append(cp=='\r' ? '\n' : cp);
        }else{
            location.consume(cp);
            if(buffer.isBufferring())
                buffer.append(cp);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected final void consumeLookAhead(int count){
        while(count-->0){
            int cp = stream.charAt(0);
            lookAhead.consumed();
            if(coelsceNewLines){
                if(location.consume(cp) && buffer.isBufferring())
                    buffer.append(cp=='\r' ? '\n' : cp);
            }else{
                location.consume(cp);
                if(buffer.isBufferring())
                    buffer.append(cp);
            }
        }
    }

    protected void addToLookAhead(int cp){
        if(lookAhead.add(cp))
            position += increment;
    }

    public boolean stop;
    public int consume(char chars[], int position, int limit) throws IOException{
        if(SHOW_STATS){
            chunkCount++;
            if(chars!=null)
                System.out.println("chunk["+chunkCount+"] = {"+new String(chars, position, limit-position)+'}');
        }
        try{
            stop = false;
            input = chars;
            this.position = position;
            this.limit = limit;

            int rule = stack[free-2];
            curState = stack[free-1];
            free -= 2;
            while(callRule(rule)){
                if(free==0){
                    int cp = codePoint();
                    if(cp==EOC)
                        return this.position;
                    else if(cp==EOF)
                        this.position = 1;
                    else
                        expected(cp, "<EOF>");
                    break;
                }
                rule = stack[free-2];
                curState = stack[free-1];
                free -= 2;
            }

            if(exitFree>0){
                if(free+exitFree>stack.length)
                    stack = Arrays.copyOf(stack, Math.max(free+exitFree, free*2));
                do{
                    free += 2;
                    stack[free-2] = exitStack[exitFree-2];
                    stack[free-1] = exitStack[exitFree-1];
                    exitFree -=2;
                }while(exitFree!=0);
            }

            if(chars==null && this.position==limit)
                onSuccessful();

            return this.position;
        }catch(IOException ex){
            throw ex;
        }catch(Exception ex){
            if(ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            else
                throw new IOException(ex);
        }
    }

    public void eof() throws IOException{
        consume(null, 0, 1);
    }

    protected abstract boolean callRule(int rule) throws Exception;

    protected void expected(int ch, String... matchers) throws Exception{
        String found;
        if(stream.length()>0)
            found = stream.toString();
        else{
            if(ch==-1)
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
    protected int curState;
    protected int stack[] = new int[100];

    protected void ioError(String message) throws IOException{
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
    protected void exiting(int rule, int state){
        exitFree += 2;
        if(exitFree>exitStack.length)
            exitStack = Arrays.copyOf(exitStack, exitFree*2);
        exitStack[exitFree-2] = rule;
        exitStack[exitFree-1] = state;
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/

    public static final int RULE_DYNAMIC_STRING_MATCH = Integer.MIN_VALUE;
    public char[] dynamicStringToBeMatched;

    protected final boolean matchString(char expected[]) throws Exception{
        int length = expected.length;

        for(int i=curState; i<length;){
            int cp = codePoint();
            int expectedCP = Character.codePointAt(expected, i);
            if(cp!=expectedCP){
                if(cp==EOC){
                    curState = i;
                    exiting(RULE_DYNAMIC_STRING_MATCH, i);
                    return false;
                }
                expected(cp, new String(new int[]{ expectedCP }, 0, 1));
            }
            consume(cp);
            i += cp<MIN_SUPPLEMENTARY_CODE_POINT ? 1 : 2;
        }

        curState = -1;
        return true;
    }

    protected final boolean matchString(int rule, int expected[]) throws Exception{
        int length = expected.length;

        for(int i=curState; i<length; i++){
            int cp = codePoint();
            if(cp!=expected[i]){
                if(cp==EOC){
                    curState = i;
                    exiting(rule, i);
                    return false;
                }
                expected(cp, new String(expected, i, 1));
            }
            consume(cp);
        }

        curState = -1;
        return true;
    }

    protected final int finishAll(int ch, int expected) throws IOException{
        while(ch>=0 && ch==expected){
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
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
    public static final boolean SHOW_STATS = true;
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
    
    private int increment;
    protected final int codePoint() throws IOException{
        int cp  = lookAhead.getNext();
        if(cp!=-2)
            return cp;
        if(position==limit){
            assert input!=null;
            return -2;
        }

        if(input==null){
            increment = 1;
            return -1;
        }

        char ch0 = input[position];
        if(ch0>=MIN_HIGH_SURROGATE && ch0<=MAX_HIGH_SURROGATE){
            if(position+1==limit)
                return -2;

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
    protected final void consume(int cp){
        if(stream.length()>0){
            if(cp==-2)
                cp = stream.charAt(0);
            lookAhead.consumed();
        }else
            position += increment;

        if(cp!=-1){
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

    @SuppressWarnings({"UnusedDeclaration"})
    protected final void consumeLookAhead(int count){
        while(count-->0){
            int cp = stream.charAt(0);
            lookAhead.consumed();
            if(cp!=-1){
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

            if(free==0){
                int cp = codePoint();
                if(cp==-1)
                    return 1;
                else
                    expected(cp, "<EOF>");
            }
            
            while(callRule()){
                if(stack[free-1]<0)
                    lookAhead.reset();
                while(free!=0 && stack[free-1]<0)
                    free -= 2;
                if(free==0){
                    int cp = codePoint();
                    if(cp==-1)
                        this.position = 1;
                    else
                        expected(cp, "<EOF>");
                    break;
                }
                if(stop)
                    break;
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

    protected abstract boolean callRule() throws Exception;

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

    protected int stack[] = new int[100];
    protected int free = 0;

    protected void push(int toRule, int stateAfterRule, int stateInsideRule){
        /*
        // fails "/Users/santhosh/projects/SAXTest/xmlconf/xmltest/valid/not-sa/001.xml"
        if(stateAfterRule==-1)
            free -= 2;
        else
            stack[free-1] = stateAfterRule;
        */
        stack[free-1] = stateAfterRule;

        free += 2;
        if(free>stack.length)
            stack = Arrays.copyOf(stack, free*2);
        stack[free-2] = toRule;
        stack[free-1] = stateInsideRule;
    }

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
}

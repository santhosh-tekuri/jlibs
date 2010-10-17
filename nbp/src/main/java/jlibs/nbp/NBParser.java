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
import java.nio.charset.CoderResult;

import static java.lang.Character.*;

/**
 * @author Santhosh Kumar T
 */
public abstract class NBParser{
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

    public void reset(int rule){
        wasHighSurrogate = false;
        stream.clear();
        location.reset();
        buffer.clear();
        ruleStack.clear();
        stateStack.clear();

        ruleStack.push(startingRule=rule);
        stateStack.push(0);
    }

    public void reset(){
        reset(startingRule);
    }

    private char highSurrogate;
    private boolean wasHighSurrogate;
    public void consume(char ch) throws IOException{
        if(wasHighSurrogate){
            wasHighSurrogate = false;
            if(ch>=MIN_LOW_SURROGATE && ch<=MAX_LOW_SURROGATE)
                consume(((highSurrogate - MIN_HIGH_SURROGATE) << 10) + (ch - MIN_LOW_SURROGATE) + MIN_SUPPLEMENTARY_CODE_POINT);
            else
                ioError("bad surrogate pair");
        }else if(ch>=MIN_HIGH_SURROGATE && ch<=MAX_HIGH_SURROGATE){
            highSurrogate = ch;
            wasHighSurrogate = true;
        }else
            consume((int)ch);
    }

    public void consume(int codePoint) throws IOException{
        try{
            consumed = false;
            _eat(codePoint);

            if(!consumed && stream.length()==0)
                consumed(codePoint);

            while(lookAhead.hasNext()){
                consumed = false;
                int lookAheadLen = lookAhead.length();
                _eat(lookAhead.getNext());
                if(!consumed && lookAhead.length()==lookAheadLen)
                    consumed();
            }
        }catch(IOException ex){
            throw ex;
        }catch(Exception ex){
            if(ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            else
                throw new IOException(ex);
        }
    }

    private void _eat(int ch) throws Exception{
        while(true){
            if(stateStack.isEmpty()){
                if(ch==-1){
                    onSuccessful();
                    return;
                }else
                    expected(ch, "<EOF>");
            }
            int state = callRule(ch);
            if(state==-1){
                pop();
                if(lookAhead.reset())
                    return;
            }else{
                stateStack.setPeek(state);
                return;
            }
        }
    }

    protected abstract int callRule(int ch) throws Exception;

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

    protected void consumed(){
        int ch = stream.charAt(0);
        if(ch!=-1)
            consumed(ch);
        lookAhead.consumed();
    }

    protected boolean consumed = false;
    protected void consumed(int ch){
        consumed = true;
        location.consume(ch);
        if(buffer.isBufferring())
            buffer.append(ch);
    }

    /*-------------------------------------------------[ Parsing Status ]---------------------------------------------------*/

    protected final IntStack ruleStack = new IntStack();
    protected final IntStack stateStack = new IntStack();

    protected void push(int toRule, int stateAfterRule, int stateInsideRule){
        stateStack.setPeek(stateAfterRule);
        ruleStack.push(toRule);
        stateStack.push(stateInsideRule);
    }

    protected void pop(){
        ruleStack.pop();
        stateStack.pop();
    }

    public void encodingError(CoderResult coderResult) throws IOException{
        ioError(coderResult.isMalformed() ? "Malformed Input" : "Unmappable Character");
    }

    protected void ioError(String message) throws IOException{
        try{
            fatalError(message);
            throw new IOException(message);
        }catch(Exception ex){
            throw new IOException(message);
        }
    }
}

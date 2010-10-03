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
import java.io.Writer;

/**
 * @author Santhosh Kumar T
 */
public abstract class NBParser extends Writer{
    private final Stream stream;
    protected final Stream.LookAhead lookAhead;
    public final Location location = new Location();
    protected final Buffer buffer = new Buffer();

    public NBParser(int maxLookAhead){
        stream = new Stream(maxLookAhead);
        lookAhead = stream.lookAhead;
    }

    private int startingRule;
    public void setRule(int rule){
        eofOnClose = true;
        wasHighSurrogate = false;
        stream.clear();
        location.reset();
        buffer.clear();
        ruleStack.clear();
        stateStack.clear();

        push(startingRule = rule, -1, 0);
    }

    private char highSurrogate;
    private boolean wasHighSurrogate;
    private void consume(char ch) throws IOException{
        if(Character.isHighSurrogate(ch)){
            highSurrogate = ch;
            wasHighSurrogate = true;
        }else{
            if(wasHighSurrogate){
                wasHighSurrogate = false;
                if(Character.isLowSurrogate(ch)){
                    int codePoint = Character.toCodePoint(highSurrogate, ch);
                    consume(codePoint);
                }else{
                    consume((int)highSurrogate);
                    consume((int)ch);
                }
            }else
                consume((int)ch);
        }
    }

    private void consume(int codePoint) throws IOException{
        try{
            consumed = false;
            _eat(codePoint);

            if(stream.length()==0 && !consumed)
                consumed(codePoint);

            while(stream.lookAhead.hasNext()){
                consumed = false;
                _eat(stream.lookAhead.getNext());
                if(stream.lookAhead.length()==0 && !consumed)
                    consumed();
            }

        }catch(IOException ex){
            eofOnClose = false;
            throw ex;
        }catch(Exception ex){
            eofOnClose = false;
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
                if(!stateStack.isEmpty()){
                    pop();
                    if(lookAhead.length()>0){
                        lookAhead.reset();
                        return;
                    }
                }
            }else{
                stateStack.setPeek(state);
                break;
            }
        }
    }

    protected abstract int callRule(int ch) throws Exception;

    protected void expected(int ch, String... matchers) throws Exception{
        String found;
        if(ch==-1)
            found = "<EOF>";
        else
            found = new String(Character.toChars(ch));
        
        StringBuilder buff = new StringBuilder();
        for(String matcher: matchers){
            if(buff.length()>0)
                buff.append(" OR ");
            buff.append(matcher);
        }

        String message = "Found: "+found+" Expected: "+buff.toString();
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
        if(!stateStack.isEmpty())
            stateStack.setPeek(stateAfterRule);
        ruleStack.push(toRule);
        stateStack.push(stateInsideRule);
    }

    protected void pop(){
        ruleStack.pop();
        stateStack.pop();
    }

    /*-------------------------------------------------[ writer ]---------------------------------------------------*/

    @Override
    public void write(int c) throws IOException{
        consume((char)c);
    }

    @Override
    public void write(char[] chars, int offset, int length) throws IOException{
        while(length>0){
            consume(chars[offset]);
            offset++;
            length--;
        }
    }

    @Override
    public Writer append(char ch) throws IOException{
        consume(ch);
        return this;
    }

    @Override
    public void write(String str, int offset, int length) throws IOException{
        while(length>0){
            consume(str.charAt(offset));
            offset++;
            length--;
        }
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException{
        while(start<end){
            consume(csq.charAt(start));
            start++;
        }
        return this;
    }

    private boolean eofOnClose = true;
    
    @Override
    public void close() throws IOException{
        if(eofOnClose)
            consume(-1);
        setRule(startingRule);
    }

    @Override
    public void flush() throws IOException{}
}

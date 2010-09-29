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

/**
 * @author Santhosh Kumar T
 */
public abstract class NBParser{
    private final Stream stream;
    protected final Stream.LookAhead lookAhead;
    public final Location location = new Location();
    protected final Buffer buffer = new Buffer();

    public NBParser(int maxLookAhead){
        stream = new Stream(maxLookAhead);
        lookAhead = stream.lookAhead;
    }

    public void reset(){
        wasHighSurrogate = false;
        stream.clear();
        location.reset();
        buffer.clear();
        ruleStack.clear();
        stateStack.clear();
    }

    public void startParsing(int rule){
        reset();
        push(rule, -1, 0);
    }

    private char highSurrogate;
    private boolean wasHighSurrogate;
    public void consume(char ch) throws java.text.ParseException{
        if(Character.isHighSurrogate(ch)){
            highSurrogate = ch;
            wasHighSurrogate = true;
        }else{
            int codePoint;
            if(wasHighSurrogate){
                codePoint = Character.toCodePoint(highSurrogate, ch);
                wasHighSurrogate = false;
            }else
                codePoint = ch;
            eat(codePoint, false);
        }
    }

    public void consume(int codePoint) throws java.text.ParseException{
        eat(codePoint, false);
    }

    public void eof() throws java.text.ParseException{
        eat('\0', true);
    }

    private void eat(int ch, boolean eof) throws java.text.ParseException{
        consumed = false;
        _eat(ch, eof);

        if(stream.length()==0 && !consumed)
            consumed(ch);

        while(stream.lookAhead.hasNext()){
            consumed = false;
            _eat(stream.lookAhead.getNext(), stream.lookAhead.isNextEOF());
            if(stream.lookAhead.length()==0 && !consumed)
                consumed();
        }
    }

    private void _eat(int ch, boolean eof) throws java.text.ParseException{
        while(true){
            if(stateStack.isEmpty()){
                if(eof)
                    return;
                else
                    expected(ch, eof, "<EOF>");
            }
            int state = callRule(ch, eof);
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

    protected abstract int callRule(int ch, boolean eof) throws java.text.ParseException;

    protected void expected(int ch, boolean eof, String... matchers) throws java.text.ParseException{
        String found;
        if(eof)
            found = "<EOF>";
        else
            found = new String(Character.toChars(ch));
        
        StringBuilder buff = new StringBuilder();
        for(String matcher: matchers){
            if(buff.length()>0)
                buff.append(" OR ");
            buff.append(matcher);
        }
        throw new java.text.ParseException("Found: "+found+" Expected: "+buff.toString(), location.getCharacterOffset());
    }

    protected void consumed(){
        if(!stream.isEOF(0))
            consumed(stream.charAt(0));
        lookAhead.consumed();
    }

    boolean consumed = false;
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
}

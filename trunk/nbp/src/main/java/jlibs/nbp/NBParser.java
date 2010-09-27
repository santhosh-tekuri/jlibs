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

    public void consume(char ch) throws java.text.ParseException{
        eat(ch, false);
    }

    public void eof() throws java.text.ParseException{
        eat('\0', true);
    }

    private void eat(char ch, boolean eof) throws java.text.ParseException{
        _eat(ch, eof);

        if(stream.length()==0)
            consumed(ch);

        while(stream.lookAhead.hasNext()){
            _eat(stream.lookAhead.getNext(), stream.lookAhead.isNextEOF());
            if(stream.lookAhead.length()==0)
                consumed();
        }
    }

    private void _eat(char ch, boolean eof) throws java.text.ParseException{
        while(true){
            if(stateStack.isEmpty()){
                if(eof)
                    return;
                else
                    expected(ch, eof, "<EOF>");
            }
            int state = callRule(ch, eof);
            if(state==-1){
                if(!stateStack.isEmpty())
                    pop();
            }else{
                stateStack.setPeek(state);
                break;
            }
        }
    }

    protected abstract int callRule(char ch, boolean eof) throws java.text.ParseException;

    protected void expected(char ch, boolean eof, String... matchers) throws java.text.ParseException{
        String found;
        if(eof)
            found = "<EOF>";
        else
            found = String.valueOf(ch);
        
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
        lookAhead.reset();
    }

    protected void consumed(char ch){
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

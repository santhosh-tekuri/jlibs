/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.xml.sax.sniff.model;

/**
 * @author Santhosh Kumar T
 */
public class LiteralNode extends Node{
    public String literal;

    public LiteralNode(String literal){
        this.literal = literal;
    }

    @Override
    public ResultType resultType(){
        return ResultType.STRING;
    }

    @Override
    public boolean equivalent(Node node){
        if(node.getClass()==getClass()){
            LiteralNode that = (LiteralNode)node;
            return that.literal.equals(this.literal);
        }else
            return false;
    }

    @Override
    public String toString(){
        return "'"+literal+"'";
    }

    @Override
    public void reset(){
        super.reset();
        addResult(-1, literal);
    }
}

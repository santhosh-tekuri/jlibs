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

package jlibs.core.nio;

/**
 * @author Santhosh Kumar T
 */
class Debuggable{
    protected static final boolean DEBUG = false;

    protected void println(String msg){
        Indentation indent = indentation.get();
        if(msg.equals("}"))
            indent.decrement();
        indent.print();
        System.out.println(msg);
        if(msg.endsWith("{"))
            indent.increment();
    }

    private static final ThreadLocal<Indentation> indentation = new ThreadLocal<Indentation>(){
        @Override
        protected Indentation initialValue(){
            return new Indentation();
        }
    };

    private static class Indentation{
        private int amount;

        public void increment(){
            amount++;
        }

        public void decrement(){
            amount--;
        }

        public void print(){
            for(int i=0; i<amount; i++)
                System.out.print("  ");
        }
    }
}

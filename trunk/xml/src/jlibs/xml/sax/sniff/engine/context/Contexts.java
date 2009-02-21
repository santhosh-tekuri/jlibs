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

package jlibs.xml.sax.sniff.engine.context;

import jlibs.core.util.ResettableIterator;
import jlibs.xml.sax.sniff.Debuggable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
class Contexts implements Iterable<Context>, Debuggable{
    public List<Context> current = new ArrayList<Context>();
    private List<Context> next = new ArrayList<Context>();

    public void reset(Context rootContext){
        current.clear();
        next.clear();
        current.add(rootContext);
    }

    ResettableIterator<Context> iter = new ResettableIterator<Context>(current);
    @Override
    public Iterator<Context> iterator(){
        return iter.reset(current);
    }

    public boolean hasAttributeChild;
    public boolean nextHasAttributeChild;
    public boolean hasNamespaceChild;
    public boolean nextHasNamespaceChild;
    public void add(Context context){
        if(context!=null){
            nextHasAttributeChild |= context.node.hasAttibuteChild && context.depth<=0;
            nextHasNamespaceChild |= context.node.hasNamespaceChild && context.depth<=0;
            next.add(context);
        }
    }

    public void addUnique(Context context){
        if(debug)
            debugger.println("     "+context);
        
        if(!next.contains(context))
            add(context);
    }

    public void update(){
        List<Context> temp = current;
        current = next;
        next = temp;
        next.clear();

        hasAttributeChild = nextHasAttributeChild;
        hasNamespaceChild = nextHasNamespaceChild;
        nextHasAttributeChild = nextHasNamespaceChild = false;

        if(debug)
            printCurrent("newContext");
    }

    private int mark;

    public void mark(){
        mark = next.size();
    }

    public int markSize(){
        return next.size()-mark;
    }

    /*-------------------------------------------------[ Debug ]---------------------------------------------------*/

    private void println(String message, List<Context> contexts, int from){
        System.out.println(message+" ->");
        if(contexts.size()==from)
            return;
        Iterator iter = contexts.listIterator(from);
        while(iter.hasNext())
            System.out.println("     "+iter.next());
        System.out.println();
    }

    public void printCurrent(String message){
        println(message, current, 0);
    }

    public void printNext(String message){
        println(message, next, mark);
    }
}

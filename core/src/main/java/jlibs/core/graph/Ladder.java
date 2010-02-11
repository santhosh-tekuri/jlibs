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

package jlibs.core.graph;

/**
 * @author Santhosh Kumar T
 */
public abstract class Ladder<E>{
    public abstract E parent(E elem);

    public int getHeight(E fromElem, E toElem){
        if(fromElem==null)
            return -1;

        int ht = 0;
        while(fromElem!=toElem){
            fromElem = parent(fromElem);
            ht++;
        }
        return ht;
    }

    public int getHeight(E elem){
        return getHeight(elem, null);
    }

    @SuppressWarnings("unchecked")
    public <A extends E> A getParent(E elem, Class<A> clazz){
        if(elem==null)
            return null;

        do{
            elem = parent(elem);
        }while(elem!=null && !clazz.isInstance(elem));

        return (A)elem;
    }

    @SuppressWarnings("unchecked")
    public <A extends E> A getAncestor(E elem, Class<A> clazz){
        if(clazz.isInstance(elem))
            return (A)elem;
        else
            return getParent(elem, clazz);
    }

    public E getRoot(E elem){
        if(elem==null)
            return null;

        E parent = parent(elem);
        while(parent!=null){
            elem = parent;
            parent = parent(elem);
        }
        return elem;
    }

    public E getSharedAncestor(E elem1, E elem2){
        if(elem1==elem2)
            return elem1;
        if(elem1==null || elem2==null)
            return null;

        int ht1 = getHeight(elem1);
        int ht2 = getHeight(elem2);

        int diff;
        if(ht1>ht2)
            diff = ht1 - ht2;
        else{
            diff = ht2 - ht1;
            E temp = elem1;
            elem1 = elem2;
            elem2 = temp;
        }

        // Go up the tree until the nodes are at the same level
        while(diff>0){
            elem1 = parent(elem1);
            diff--;
        }

        // Move up the tree until we find a common ancestor.  Since we know
        // that both nodes are at the same level, we won't cross paths
        // unknowingly (if there is a common ancestor, both nodes hit it in
        // the same iteration).
        do{
            if(elem1.equals(elem2))
                return elem1;
            elem1 = parent(elem1);
            elem2 = parent(elem2);
        }while(elem1 != null); // only need to check one -- they're at the
                               // same level so if one is null, the other is

        return null;
    }

    public boolean isAncestor(E elem, E ancestor){
        if(ancestor==null)
            return false;

        while(elem!=null){
            if(elem==ancestor)
                return true;
            elem = parent(elem);
        }
        return false;
    }

    public boolean isRelated(E elem1, E elem2){
        return !(elem1==null || elem2==null) && getRoot(elem1)==getRoot(elem2);
    }

    public String getPath(E elem, Convertor<E, String> convertor, String separator){
        StringBuilder buff = new StringBuilder();
        while(elem!=null){
            if(buff.length()>0)
                buff.insert(0, separator);
            buff.insert(0, convertor.convert(elem));
            elem = parent(elem);
        }
        return buff.toString();
    }

    public String getRelativePath(E fromElem, E toElem, Convertor<E, String> convertor, String separator){
        if(fromElem==toElem)
            return ".";

        E sharedAncestor = getSharedAncestor(fromElem, toElem);
        if(sharedAncestor==null)
            return null;

        StringBuilder buff1 = new StringBuilder();
        while(!fromElem.equals(sharedAncestor)){
            if(buff1.length()>0)
                buff1.append(separator);
            buff1.append("..");
            fromElem = parent(fromElem);
        }

        StringBuilder buff2 = new StringBuilder();
        while(!toElem.equals(sharedAncestor)){
            if(buff2.length()>0)
                buff2.insert(0, separator);

            buff2.insert(0, convertor.convert(toElem));

            toElem = parent(toElem);
        }

        if(buff1.length()>0 && buff2.length()>0)
            return buff1+separator+buff2;
        else
            return buff1.length()>0 ? buff1.toString() : buff2.toString();
    }
}

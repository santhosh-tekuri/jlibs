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

package jlibs.core.util;

import jlibs.core.lang.NotImplementedException;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Optimized TreeMap implementation whose keys are primitive long.
 * Originally copied from java.util.TreeMap and Optimized further
 *  
 * @author Santhosh Kumar T
 */
public final class LongTreeMap<V>{
    private transient Entry<V> root;
    private transient int size;

    // Red-black mechanics
    private static final boolean RED   = false;
    private static final boolean BLACK = true;

    public static final class Entry<V>{
	    long key;
        public V value;
        Entry<V> left;
        Entry<V> right;
        Entry<V> parent;
        boolean color; // RED

        Entry(long key, V value, Entry<V> parent){
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        public long getKey(){
            return key;
        }

        public Entry<V> next(){
            if(right!=null){
                Entry<V> p = right;

                Entry<V> q = p.left;
                while(q!=null){
                    p = q;
                    q = q.left;
                }
                return p;
            }else{
                Entry<V> p = parent;
                Entry<V> ch = this;
                while(p!=null && ch==p.right){
                    ch = p;
                    p = p.parent;
                }
                return p;
            }
        }

        @SuppressWarnings({"unchecked"})
        public boolean equals(Object o){
            if(o instanceof Entry){
                Entry<V> e = (Entry<V>)o;
                return valEquals(key, e.key) && valEquals(value, e.value);
            }else
                return false;
        }

        public int hashCode() {
            int keyHash = (int)(key ^ (key >>> 32));
            int valueHash = (value==null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        public String toString() {
            return key + "=" + value;
        }
    }

    private static boolean valEquals(Object o1, Object o2){
        return o1==null ? o2==null : o1.equals(o2);
    }

    private static <V> boolean colorOf(Entry<V> p){
        return p==null ? BLACK : p.color;
    }

    private static <V> void setColor(Entry<V> p, boolean c){
        if(p!=null)
	        p.color = c;
    }

    private static <V> Entry<V> leftOf(Entry<V> p){
        return p==null ? null: p.left;
    }

    private static <V> Entry<V> rightOf(Entry<V> p){
        return p==null ? null: p.right;
    }

    public Entry<V> firstEntry(){
        Entry<V> p = root;
        if(p!=null){
            Entry<V> q = p.left;
            while(q!=null){
                p = q;
                q = q.left;
            }
        }
        return p;
    }

    public Entry<V> lastEntry(){
        Entry<V> p = root;
        if(p!=null){
            Entry<V> q = p.right;
            while(q!=null){
                p = q;
                q = q.right;
            }
        }
        return p;
    }

    private Entry<V> rotateLeft(final Entry<V> p){
        final Entry<V> r = p.right;
        Entry<V> rLeft = r.left;
        p.right = rLeft;
        if(rLeft!=null)
            rLeft.parent = p;
        Entry<V> pParent = p.parent;
        r.parent = pParent;
        if(pParent==null)
            root = r;
        else if(pParent.left==p)
            pParent.left = r;
        else
            pParent.right = r;
        r.left = p;
        return p.parent = r;
    }

    private Entry<V> rotateRight(final Entry<V> p){
        Entry<V> l = p.left;
        Entry<V> lRight = l.right;
        p.left = lRight;
        if(lRight!=null)
            lRight.parent = p;
        Entry<V> pParent = p.parent;
        l.parent = pParent;
        if(pParent==null)
            root = l;
        else if(pParent.right==p)
            pParent.right = l;
        else
            pParent.left = l;
        l.right = p;
        return p.parent = l;
    }

    private void fixAfterInsertion(Entry<V> x){
        do{
            Entry<V> px = x.parent;
            Entry<V> ppx = px.parent;
            Entry<V> ppxLeft = ppx!=null ? ppx.left : null;
            if(px==ppxLeft){
                if(ppx.right!=null && !ppx.right.color){ // !colorOf(ppxRight)
                    px.color = BLACK;
                    ppx.color = RED;
                    ppx.right.color = BLACK;
                    x = ppx;
                }else{
                    if(x==px.right){
                        x = px;
                        px = rotateLeft(x); // ppx wont change after this
                    }
                    px.color = BLACK;
                    ppx.color = RED;
                    rotateRight(ppx);
                }
            }else{
                if(ppxLeft!=null && !ppxLeft.color){ // !colorOf(ppxLeft)
                    px.color = BLACK;
                    ppx.color = RED;
                    ppxLeft.color = BLACK;
                    x = ppx;
                }else{
                    if(x==px.left){
                        x = px;
                        px = rotateRight(x);  // ppx wont change after this
                    }
                    px.color = BLACK;
                    if(ppx!=null){
                        ppx.color = RED;
                        rotateLeft(ppx);
                    }
                }
            }
        }while(x!=root && !x.parent.color);
        root.color = BLACK;
    }

    private void fixAfterDeletion(Entry<V> x){
        while(x!=root && x.color){
            Entry<V> px = x.parent;
            Entry<V> pxLeft = px.left;
            if(x==pxLeft){
                Entry<V> sib = px.right;

                if(sib!=null && !sib.color){ // !colorOf(sib)
                    sib.color = BLACK;
                    px.color = RED;
                    rotateLeft(px);
                    sib = rightOf(x.parent);
                }

                Entry<V> sibLeft = sib!=null ? sib.left : null;
                Entry<V> sibRight = sib!=null ? sib.right : null;
                boolean sibRightColor = sibRight==null || sibRight.color;
                if(colorOf(sibLeft) && sibRightColor){
                    setColor(sib, RED);
                    x = x.parent;
                }else{
                    if(sibRightColor){
                        if(sib!=null){
                            setColor(sibLeft, BLACK);
                            sib.color = RED;
                            rotateRight(sib);
                        }
                        sib = rightOf(x.parent);
                    }
                    px = x.parent;
                    if(sib!=null){
                        sib.color = colorOf(px);
                        setColor(sib.right, BLACK);
                    }
                    if(px!=null){
                        px.color = BLACK;
                        rotateLeft(px);
                    }
                    x = root;
                }
            }else{ // symmetric
                Entry<V> sib = pxLeft;

                if(sib!=null && !sib.color){ // !colorOf(sib)
                    sib.color = BLACK;
                    px.color = RED;
                    rotateRight(px);
                    sib = leftOf(x.parent);
                }

                Entry<V> sibLeft = sib!=null ? sib.left : null;
                Entry<V> sibRight = sib!=null ? sib.right : null;
                boolean sibLeftColor = sibLeft==null || sibLeft.color;
                if(colorOf(sibRight) && sibLeftColor){
                    setColor(sib, RED);
                    x = x.parent;
                }else{
                    if(sibLeftColor){
                        if(sib!=null){
                            setColor(sibRight, BLACK);
                            sib.color = RED;
                            rotateLeft(sib);
                        }
                        sib = leftOf(x.parent);
                    }
                    px = x.parent;
                    if(sib!=null){
                        sib.color = colorOf(px);
                        setColor(sib.left, BLACK);
                    }
                    if(px!=null){
                        px.color = BLACK;
                        rotateRight(px);
                    }
                    x = root;
                }
            }
        }

        if(x!=null)
            x.color = BLACK;
    }

    public int size(){
        return size;
    }

    public boolean isEmpty(){
        return size==0;
    }

    public V put(long key, V value){
        assert value!=null;

        Entry<V> t = root;
        if(t==null){
            root = new Entry<V>(key, value, null);
            root.color = BLACK;
            size = 1;
            return null;
        }

        boolean cmp;
        Entry<V> parent;
        do{
            parent = t;
            long tkey = t.key;
            if(key<tkey){
                t = t.left;
                cmp = true;
            }else if(key>tkey){
                t = t.right;
                cmp = false;
            }else{
                V oldValue = t.value;
                t.value = value;
                return oldValue;
            }
        }while(t!=null);

        Entry<V> e = new Entry<V>(key, value, parent);
        if(cmp)
            parent.left = e;
        else
            parent.right = e;

        if(!parent.color)
            fixAfterInsertion(e);

        size++;
        return null;
    }

    public void deleteEntry(Entry<V> p){
        size--;

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if(p.left!=null && p.right!=null){
            Entry<V> s = p.next();
            p.key = s.key;
            p.value = s.value;
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        Entry<V> replacement = (p.left!=null ? p.left : p.right);
        Entry<V> pp = p.parent;

        if(replacement!=null){
            // Link replacement to parent
            replacement.parent = pp;
            if(pp==null)
                root = replacement;
            else if(p==pp.left)
                pp.left = replacement;
            else
                pp.right = replacement;

            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = null;

            // Fix replacement
            if(p.color)
                fixAfterDeletion(replacement);
        }else if(pp==null) // return if we are the only node.
            root = null;
        else{ //  No children. Use self as phantom replacement and unlink.
            if(p.color)
                fixAfterDeletion(p);

            pp = p.parent;
            if(pp!=null){
                if(p==pp.left)
                    pp.left = null;
                else if(p==pp.right)
                    pp.right = null;
                p.parent = null;
            }
        }
    }

    public V remove(long key){
        Entry<V> p = getEntry(key);
        if(p==null)
            return null;

        V oldValue = p.value;
        deleteEntry(p);
        return oldValue;
    }

    public void clear(){
        size = 0;
        root = null;
    }

    public void putAll(LongTreeMap<? extends V> map){
        Entry<? extends V> q = null;
        Entry<? extends V> p = map.root;
        do{
            // traverse down left branches as far as possible
            while(p!=null){
                q = p;
                p = p.left;
            }
            if(q!=null){
                put(q.key, q.value);
                p = q.right;
            }
            while(q!=null && p==null){
                do{
                    p = q;
                    q = p.parent;
                }while(q!=null && q.right==p);
                if(q!=null){
                    put(q.key, q.value);
                    p = q.right;
                }
            }
        }while(q!=null);
    }

    public Entry<V> getEntry(long key){
        Entry<V> p = root;
        while(p!=null){
            if(key<p.key)
                p = p.left;
            else if(key>p.key)
                p = p.right;
            else
                return p;
        }
        return null;
    }

    public V get(long key){
        Entry<V> entry = getEntry(key);
        return entry!=null ? entry.value : null;
    }

    static final class ValuesIterator<V> implements Iterator<V>{
        Entry<V> next;

        ValuesIterator(Entry<V> firstEntry){
            next = firstEntry;
        }

        @Override
        public boolean hasNext(){
            return next!=null;
        }

        @Override
        public V next(){
            if(next==null)
                throw new NoSuchElementException();
            V value = next.value;
            next = next.next();
            return value;
        }

        @Override
        public void remove(){
            throw new NotImplementedException();
        }
    }

    class Values extends AbstractCollection<V>{
        public Iterator<V> iterator(){
            return new ValuesIterator<V>(firstEntry());
        }

        public int size(){
            return size;
        }

        @Override
        public boolean contains(Object value){
            assert value!=null;

            Entry<V> q = null;
            Entry<V> p = root;
            do{
                // traverse down left branches as far as possible
                while(p!=null){
                    q = p;
                    p = p.left;
                }
                if(q!=null){
                    if(q.value.equals(value))
                        return true;
                    p = q.right;
                }
                while(q!=null && p==null){
                    do{
                        p = q;
                        q = p.parent;
                    }while(q!=null && q.right==p);
                    if(q!=null){
                        if(q.value.equals(value))
                            return true;
                        p = q.right;
                    }
                }
            }while(q!=null);

            return false;
        }

        @Override
        public Object[] toArray(){
            Object array[] = new Object[size];

            int i = 0;
            Entry<V> q = null;
            Entry<V> p = root;
            do{
                // traverse down left branches as far as possible
                while(p!=null){
                    q = p;
                    p = p.left;
                }
                if(q!=null){
                    array[i++] = q.value;
                    p = q.right;
                }
                while(q!=null && p==null){
                    do{
                        p = q;
                        q = p.parent;
                    }while(q!=null && q.right==p);
                    if(q!=null){
                        array[i++] = q.value;
                        p = q.right;
                    }
                }
            }while(q!=null);
            return array;
        }

        public void clear(){
            LongTreeMap.this.clear();
        }
    }

    private Values values;
    public Collection<V> values(){
        if(values==null)
            return values = new Values();
    	return values;
    }

    public static void main(String[] args){
        LongTreeMap<String> map = new LongTreeMap<String>();
        for(int i=0; i<10; i++)
            map.put(1, "value");
        map.put(5, "five");
        map.put(3, "three");
        map.put(9, "nine");
        System.out.println(map.firstEntry());
        System.out.println(map.lastEntry());

        for(Entry<String> entry=map.firstEntry(); entry!=null; entry=entry.next())
            System.out.println(entry);
    }
}

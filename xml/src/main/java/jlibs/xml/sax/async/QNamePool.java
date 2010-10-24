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

package jlibs.xml.sax.async;

/**
 * @author Santhosh Kumar T
 */
final class QNamePool{
    private QName buckets[];

    private int count; // total number of entries in the hash table

    /** The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).) */
    private int threshold;

    /** The load factor for the SymbolTable. */
    private final float loadFactor;

    public QNamePool(int initialCapacity, float loadFactor){
        this.loadFactor = loadFactor;
        buckets = new QName[initialCapacity];
        threshold = (int)(initialCapacity*loadFactor);
    }

    public QNamePool(){
        this(101, 0.75f);
    }

    public QName add(int prefixLength, char[] buffer, int offset, int length){
        int hash = 0;
        for(int i=0; i<length; i++)
            hash = 31*hash + buffer[offset+i];
        hash &= 0x7FFFFFF;

        // search for identical symbol
        int ibucket = hash % buckets.length;

        OUTER:
        for(QName bucket=buckets[ibucket]; bucket!=null; bucket=bucket.next){
            if(bucket.hash==hash && length==bucket.chars.length){
                char chars[] = bucket.chars;
                for(int i=0; i<length; i++){
                    if(buffer[offset+i]!=chars[i])
                        continue OUTER;
                }
                return bucket;
            }
        }

        if(count>=threshold){ // Rehash the table if the threshold is exceeded
            rehash();
            ibucket = hash % buckets.length;
        }

        // add new entry
        ++count;
        return buckets[ibucket] = new QName(prefixLength, buffer, offset, length, hash, buckets[ibucket]);
    }

    protected void rehash(){
        QName oldTable[] = buckets;
        int oldCapacity = buckets.length;

        int newCapacity = oldCapacity*2+1;
        buckets = new QName[newCapacity];
        threshold = (int)(newCapacity*loadFactor);

        for(int i=oldCapacity ;i-->0 ;){
            for(QName old = oldTable[i] ; old!=null;){
                QName b = old;
                old = old.next;
                int index = b.hash % newCapacity;
                b.next = buckets[index];
                buckets[index] = b;
            }
        }
    }
}

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
public class HitManager{
    public static final RuntimeException STOP_PARSING = new RuntimeException();
    private int min;

    public HitManager totalHits; 

    public void setMin(int minHits){
        if(minHits<0)
            minHits = Integer.MAX_VALUE;

        if(this.min==Integer.MAX_VALUE || minHits==Integer.MAX_VALUE)
            this.min = totalHits.min = Integer.MAX_VALUE;
        else{
            minHits = Math.max(this.min, minHits);
            if(this.min!=minHits){
                totalHits.min -= this.min;
                this.min = minHits;
                totalHits.min += this.min;
            }
        }

        reset();
    }

    private int pending;

    public void hit(){
        pending--;
        if(pending>=0){
            totalHits.pending--;
            if(totalHits.pending==0)
                throw STOP_PARSING;
        }
    }

    public void reset(){
        pending = min;
        totalHits.pending = totalHits.min;
    }
}

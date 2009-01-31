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

package jlibs.xml.sax.sniff.model.computed.derived.nodeset;

import jlibs.xml.sax.sniff.model.ResultType;
import jlibs.xml.sax.sniff.model.computed.CachedResults;
import org.jetbrains.annotations.NotNull;

/**
 * @author Santhosh Kumar T
 */
public class SumNodeSet extends DerivedNodeSetResults{
    @Override
    public ResultType resultType(){
        return ResultType.NUMBER;
    }

    private class SumResultCache extends ResultCache{
        private double number;
        private double pendingNumber;
        
        private double toDouble(String str){
            try{
                return Double.parseDouble(str);
            }catch(NumberFormatException nfe){
                return Double.NaN;
            }
        }

        @Override
        public void updatePending(String str){
            pendingNumber += toDouble(str);
        }

        @Override
        public void promotePending(){
            number += pendingNumber;
        }

        @Override
        public void resetPending(){
            pendingNumber = 0;
        }

        @Override
        public void promote(String str){
            number += toDouble(str);
        }

        @Override
        public void populateResults(){
            addResult(-1, String.valueOf(number));
        }
    }

    @NotNull
    @Override
    protected CachedResults createResultCache(){
        return new SumResultCache();
    }

    /*-------------------------------------------------[ ToString ]---------------------------------------------------*/

    @Override
    public String getName(){
        return "sum";
    }
}

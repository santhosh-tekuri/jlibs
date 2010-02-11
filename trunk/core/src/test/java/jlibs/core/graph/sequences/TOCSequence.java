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

package jlibs.core.graph.sequences;

/**
 * @author Santhosh Kumar T
 */
public class TOCSequence extends AbstractSequence<String>{
    private int number;
    private int count;

    public TOCSequence(int number, int count){
        this.number = number;
        this.count = count;
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int index;

    @Override
    protected String findNext(){
        index++;
        return index<=count ? number+"."+index : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        index = 0;
    }

    @Override
    public TOCSequence copy(){
        return new TOCSequence(number, count);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return count;
    }
}

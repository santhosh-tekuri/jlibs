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

import jlibs.core.lang.Flag;

/**
 * This class represents integer range.
 *
 * @author Santhosh Kumar T
 */
public class Range{
    public int start;
    public int end;

    public Range(int start, int end){
        this.start = start;
        this.end = end;
    }

    // bitwise flags used to represent positions
    public static final int POSITION_BEFORE = 1;
    public static final int POSITION_INSIDE = 2;
    public static final int POSITION_AFTER  = 4;

    public int position(Range range){
        int flag = 0;

        if(range.start<this.start)
            flag = Flag.set(flag, POSITION_BEFORE);
        if(range.end>this.end)
            flag = Flag.set(flag, POSITION_BEFORE);
        if((Flag.isSet(flag, POSITION_BEFORE) && Flag.isSet(flag, POSITION_AFTER))
                || (range.start>=this.start && range.start<this.end)
                || (range.end>this.start && range.end<this.end))
            flag = Flag.set(flag, POSITION_INSIDE);

        return flag;
    }

    public Range[] split(Range range){
        int pos = position(range);
        return new Range[]{ before(range, pos), inside(range, pos), after(range, pos)};
    }

    public Range before(Range range){
        return before(range, position(range));
    }

    private Range before(Range range, int pos){
        return Flag.isSet(pos, POSITION_BEFORE)
                            ? new Range(range.start, Math.min(range.end, this.start))
                            : null;
    }

    public Range inside(Range range){
        return inside(range, position(range));
    }

    private Range inside(Range range, int pos){
        return Flag.isSet(pos, POSITION_INSIDE)
                            ? new Range(Math.max(range.start, this.start), Math.min(range.end, this.end))
                            : null;
    }

    public Range after(Range range){
        return after(range, position(range));
    }

    private Range after(Range range, int pos){
        return Flag.isSet(pos, POSITION_AFTER)
                            ? new Range(Math.max(range.start, this.end), range.end)
                            : null;
    }
}

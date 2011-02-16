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

package jlibs.core.lang;

/**
 * @author Santhosh Kumar T
 */
public enum DurationUnit implements Count.Unit{
    NANO_SECONDS(1000000), MILLI_SECONDS(1000), SECONDS(60), MINUTES(60), HOURS(24), DAYS(30), MONTHS(12), YEARS(0);

    private int count;

    private DurationUnit(int count){
        this.count = count;
    }

    @Override
    public int count(){
        return count;
    }
}

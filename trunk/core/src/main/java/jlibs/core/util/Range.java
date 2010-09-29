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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents integer range.
 *
 * @author Santhosh Kumar T
 */
public class Range{
    public final int min;
    public final int max;

    /**
     * Note that both min and max are inclusive
     */
    public Range(int min, int max){
        if(min>max)
            throw new IllegalArgumentException("invalid range: ["+min+", "+max+']');
        this.min = min;
        this.max = max;
    }

    @Override
    public int hashCode(){
        return min+max;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Range){
            Range that = (Range)obj;
            return this.min==that.min && this.max==that.max;
        }else
            return false;
    }

    @Override
    public String toString(){
        return "["+min+", "+max+']';
    }

    /**
     * return true if this range is before given number
     * <pre>
     * ------------   o
     * </pre>
     */
    public boolean before(int x){
        return x>max;
    }

    /**
     * returns true if this range is after given number
     * <pre>
     *   o   --------------
     * </pre>
     */
    public boolean after(int x){
        return x<min;
    }

    /**
     * returns true if this range contains given number
     * <pre>
     *      ------o--------
     * </pre>
     */
    public boolean contains(int x){
        return !before(x) && !after(x);
    }

    /**
     * tells the position this range with respect to given range.
     * <p><br>
     * the return value is boolean array of size 3.<br>
     * <blockquote>
     *   1st boolean ==> true if some portion of this range is before given range<br>
     *   2nd boolean ==> true if this range intersects with given range<br>
     *   3rd boolean ==> true if some portion of this range is after given range
     * </blockquote>
     * ideally, you can remebers these boolean as { before, inside, after }
     */
    public boolean[] position(Range that){
        boolean before = that.after(min);
        boolean after = that.before(max);

        boolean inside;
        if(before && after)
            inside = true;
        else if(!before && !after)
            inside = true;
        else if(before)
            inside = that.contains(max);
        else
            inside = that.contains(min);
        return new boolean[]{ before, inside, after };
    }

    /**
     * this method splits this range into 3 regions with respect to given range
     * <p><br>
     * the return value is Range array of size 3.<br>
     * <blockquote>
     *  1st range ==> the portion of this range that is before given range<br>
     *  2nd range ==> the portion of range that is common to this range and given range<br>
     *  3rd range ==> the portion of this range that is after given range.
     * </blockquote>
     * Note that the values in returned array can be null, if there is no range satifying
     * the requirement.
     */
    public Range[] split(Range that){
        boolean position[] = position(that);

        Range before = null;
        if(position[0])
            before = new Range(this.min,  Math.min(this.max, that.min-1));

        Range after = null;
        if(position[2])
            after = new Range(Math.max(this.min, that.max+1), this.max);

        Range inside = null;
        if(position[1])
            inside = new Range(Math.max(this.min, that.min), Math.min(this.max, that.max));

        return new Range[]{ before, inside, after };
    }

    /**
     * return the portion of range that is common to this range and given range.<br>
     * If there is nothing common, then null is returned.
     */
    public Range intersection(Range that){
        return split(that)[1];
    }

    /**
     * returns union of this range with given range.<br>
     * if both ranges are adjacent/intersecting to each other, then the
     * returned array will have only one range. otherwise the returned array
     * will have two ranges in sorted order. 
     */
    public Range[] union(Range that){
        if(this.min>that.min)
            return that.union(this);

        // this -------------
        // that    --------------
        // that    -------
        if(contains(that.min))
            return new Range[]{ new Range(this.min, Math.max(this.max, that.max)) };

        // this -----------
        // that                --------------
        return new Range[]{ this, that };
    }

    /**
     * returns the portion(s) of this range that are not present in given range.
     * maximum size of returned array is 2. and the array is sorted.
     */
    public Range[] minus(Range that){
        Range split[] = split(that);
        if(split[0]==null && split[2]==null)
            return new Range[0];
        if(split[0]!=null && split[2]!=null)
            return new Range[]{ split[0], split[2] };

        return new Range[]{ split[0]!=null ? split[0] : split[2] };
    }

    /*-------------------------------------------------[ Helpers ]---------------------------------------------------*/
    
    public static List<Range> union(List<Range> ranges){
        ranges = new ArrayList<Range>(ranges);
        Collections.sort(ranges, new Comparator<Range>(){
            @Override
            public int compare(Range r1, Range r2){
                return r1.min-r2.min;
            }
        });
        List<Range> union = new ArrayList<Range>();
        for(Range r: ranges){
            if(union.isEmpty())
                union.add(r);
            else
                CollectionUtil.addAll(union, union.remove(union.size() - 1).union(r));
        }
        return union;
    }

    public static List<Range> intersection(List<Range> list1, List<Range> list2){
        list1 = union(list1);
        list2 = union(list2);
        List<Range> intersection = new ArrayList<Range>();
        for(Range r1: list1){
            for(Range r2: list2){
                Range r = r1.intersection(r2);
                if(r!=null)
                    intersection.add(r);
            }
        }
        return union(intersection);
    }

    public static List<Range> minus(List<Range> list1, List<Range> list2){
        list1 = union(list1);
        list2 = union(list2);

        for(Range r2: list2){
            List<Range> temp = new ArrayList<Range>();
            for(Range r1: list1)
                CollectionUtil.addAll(temp, r1.minus(r2));
            list1 = temp;
        }
        return union(list1);
    }

    public static boolean same(List<Range> list1, List<Range> list2){
        list1 = union(list1);
        list2 = union(list2);
        return list1.equals(list2);
    }
}

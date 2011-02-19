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

import java.util.Arrays;

/**
 * Class used to count by units
 *
 * @author Santhosh Kumar T
 */
public class Count<T extends Count.Unit> implements Comparable<Count<T>>{
    private long amounts[];
    private T units[];

    private Count(T units[]){
        this.units = units;
        amounts = new long[units.length];
    }

    static interface Unit{
        public int ordinal();
        public int count();
        public String toString();
    }

    public static <T extends Unit> Count<T> newInstance(Class<T> unitClass){
        if(!unitClass.isEnum())
            throw new IllegalArgumentException(unitClass+" should be enum");
        return new Count<T>(unitClass.getEnumConstants());
    }

    /*-------------------------------------------------[ Manipulation ]---------------------------------------------------*/

    public Count<T> add(long amount, T unit){
        if(amount<0)
            throw new IllegalArgumentException("amount '"+amount+"' should be >=0");
        if(amount>0){
            amounts[unit.ordinal()] += amount;
            for(int i=unit.ordinal(); i<units.length-1; i++){
                if(amounts[i]>=units[i].count()){
                    amounts[i+1] += amounts[i] / units[i].count();
                    amounts[i] %= units[i].count();
                }else
                    break;
            }
        }
        return this;
    }

    public Count<T> add(Count<T> other){
        for(T unit: units)
            add(other.amounts[unit.ordinal()], unit);
        return this;
    }

    public Count<T> clear(){
        Arrays.fill(amounts, 0);
        return this;
    }

    public Count<T> set(long amount, T unit){
        if(amount<0)
            throw new IllegalArgumentException("amount '"+amount+"' should be >=0");
        return clear().add(amount, unit);
    }

    public Count<T> set(Count<T> other){
        return clear().add(other);
    }

    /*-------------------------------------------------[ Getters ]---------------------------------------------------*/

    public long get(T unit){
        return amounts[unit.ordinal()];
    }

    public strictfp double to(T unit){
        long value = amounts[units.length-1];
        for(int i=units.length-2; i>=unit.ordinal(); i--)
            value = amounts[i]+ units[i].count()*value;
        if(unit.ordinal()>0){
            double before = amounts[0];
            for(int i=1; i<unit.ordinal(); i++)
                before = before/units[i-1].count() + amounts[i];
            return value + before/units[unit.ordinal()-1].count();
        }
        return value;
    }

    /*-------------------------------------------------[ Object ]---------------------------------------------------*/

    @Override
    public int hashCode(){
        long hash = 0;
        for(long amount: amounts)
            hash =31*hash + amount;
        return (int)(hash ^ hash>>>32);
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Count){
            Count<?> that = (Count<?>)obj;
            return Arrays.equals(this.units, that.units) && Arrays.equals(this.amounts, that.amounts);
        }
        return false;
    }

    public String toString(){
        StringBuilder buff = new StringBuilder();

        for(int i=units.length-1; i>=0; i--){
            if(amounts[i]>0){
                if(buff.length()>0)
                    buff.append(' ');
                buff.append(amounts[i]);
                buff.append(' ');
                buff.append(units[i]);
            }
        }
        return buff.length()==0 ? "0 "+units[0] : buff.toString();
    }

    /*-------------------------------------------------[ Comparable ]---------------------------------------------------*/

    @Override
    public int compareTo(Count<T> that){
        for(int i=units.length-1; i>=0; i--){
            if(this.amounts[i]!=that.amounts[i])
                return this.amounts[i]<that.amounts[i] ? -1 : +1;
        }
        return 0;
    }
}

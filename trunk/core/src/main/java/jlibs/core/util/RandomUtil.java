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

/**
 * @author Santhosh Kumar T
 */
public class RandomUtil{
    public static double random(double min, double max){
        return min+Math.random()*(max-min);
    }

    public static float random(float min, float max){
        return (float)(min+Math.random()*(max-min));
    }

    public static long random(long min, long max){
        return Math.round(min+Math.random()*(max-min));
    }

    public static int random(int min, int max){
        return (int)Math.round(min+Math.random()*(max-min));
    }

    public static short random(short min, short max){
        return (short)Math.round(min+Math.random()*(max-min));
    }

    public static byte random(byte min, byte max){
        return (byte)Math.round(min+Math.random()*(max-min));
    }

    public static boolean randomBoolean(){
        return Math.random()<0.5d;
    }

    public static boolean randomBoolean(Boolean bool){
        if(Boolean.TRUE.equals(bool))
            return true;
        else if(Boolean.FALSE.equals(bool))
            return false;
        else // random either 0 or 1
            return randomBoolean();
    }
}

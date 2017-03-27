/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.core.util;

/**
 * @author Santhosh Kumar T
 */
public class RandomUtil{
    public static double random(double min, double max){
        // see http://stackoverflow.com/a/9724775
        return Math.random()<0.5
                ? ((1-Math.random()) * (max-min) + min)
                : (Math.random() * (max-min) + min);
    }

    public static float random(float min, float max){
        // see http://stackoverflow.com/a/9724775
        return Math.random()<0.5
                ? (float)((1-Math.random()) * (max-min) + min)
                : (float)(Math.random() * (max-min) + min);

    }

    public static long random(long min, long max){
        return Math.round(min+Math.random()*(max-min));
    }

    public static int random(int min, int max){
        Random r = new Random();
        return r.nextInt(max);
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

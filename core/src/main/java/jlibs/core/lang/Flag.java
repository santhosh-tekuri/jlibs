/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
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

package jlibs.core.lang;

/**
 * This class contains helper methods for working with
 * bitwise flags
 * 
 * @author Santhosh Kumar T
 */
public class Flag{
    /*-------------------------------------------------[ int ]---------------------------------------------------*/

    public static int set(int value, int flag){
        return value | flag;
    }

    public static boolean isSet(int value, int flag){
        return (value & flag)!=0;
    }

    public static int unset(int value, int flag){
        return value & ~flag;
    }

    public static int toggle(int value, int flag){
        return value ^ ~flag;
    }

    /*-------------------------------------------------[ long ]---------------------------------------------------*/

    public static long set(long value, long flag){
        return value | flag;
    }

    public static boolean isSet(long value, long flag){
        return (value & flag)!=0;
    }

    public static long unset(long value, long flag){
        return value & ~flag;
    }

    public static long toggle(long value, long flag){
        return value ^ ~flag;
    }
}

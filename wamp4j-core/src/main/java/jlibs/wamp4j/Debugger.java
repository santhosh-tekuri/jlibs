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

package jlibs.wamp4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Debugger{
    public static final boolean CLIENT = false;
    public static final boolean ROUTER = false;

    public static void println(Object module, String message, Object... args){
        List<Object> list = new ArrayList<Object>(1+args.length);
        list.add(module);
        list.add(Arrays.asList(args));
        System.out.printf("%20s "+message+"%n", list.toArray());
    }
}

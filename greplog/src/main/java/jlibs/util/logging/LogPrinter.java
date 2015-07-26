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

package jlibs.util.logging;

/**
 * @author Santhosh Kumar T
 */
public class LogPrinter implements LogHandler{
    public final boolean printIndex;

    public LogPrinter(boolean printIndex){
        this.printIndex = printIndex;
    }

    @Override
    public void consume(LogRecord record){
        if(printIndex){
            System.out.print(record.index);
            System.out.print(' ');
        }
        System.out.print(record.fields[0]);
        System.out.println(record.message);
    }
}

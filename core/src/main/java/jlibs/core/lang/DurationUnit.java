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

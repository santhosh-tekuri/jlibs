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
public class PrecedingCondition implements Condition{
    public final Condition condition;
    public final boolean includeSelf;

    public PrecedingCondition(Condition condition, boolean includeSelf){
        this.condition = condition;
        this.includeSelf = includeSelf;
    }

    private boolean matched;

    @Override
    public boolean matches(LogRecord record){
        if(matched)
            return false;
        if(condition.matches(record)){
            matched = true;
            return includeSelf;
        }else
            return true;
    }
}

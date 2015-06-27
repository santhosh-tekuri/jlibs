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

package jlibs.nblr.matchers;

import jlibs.core.util.Range;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Or extends Matcher{
    public final Matcher operands[];

    public Or(Matcher... operands){
        this.operands = operands;
    }

    @Override
    public boolean hasCustomJavaCode(){
        if(super.hasCustomJavaCode())
            return true;
        for(Matcher operand: operands){
            if(operand.hasCustomJavaCode())
                return true;
        }
        return false;
    }

    @Override
    protected String __javaCode(String variable){
        StringBuilder buff = new StringBuilder();
        for(Matcher operand: operands){
            if(buff.length()>0)
                buff.append(" || ");
            buff.append('(').append(operand._javaCode(variable)).append(')');
        }
        return buff.toString();
    }

    @Override
    public List<Range> ranges(){
        List<Range> ranges = new ArrayList<Range>();
        for(Matcher operand: operands)
            ranges.addAll(operand.ranges());
        return Range.union(ranges);
    }

    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        for(Matcher operand: operands){
            String msg = operand._toString();
            if(buff.length()==0 || !msg.startsWith("[^"))
                msg = msg.substring(1, msg.length()-1);
            buff.append(msg);
        }
        return '['+buff.toString()+']';
    }
}

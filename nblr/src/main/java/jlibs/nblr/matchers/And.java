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

package jlibs.nblr.matchers;

import jlibs.core.util.Range;

import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class And extends Matcher{
    public final Matcher operands[];

    public And(Matcher... operands){
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
                buff.append(" && ");
            buff.append('(').append(operand._javaCode(variable)).append(')');
        }
        return buff.toString();
    }

    @Override
    public List<Range> ranges(){
        List<Range> ranges = null;
        for(Matcher operand: operands){
            List<Range> operandRanges = operand.ranges();
            if(ranges==null)
                ranges = operandRanges;
            else{
                ranges = Range.intersection(ranges, operandRanges);
            }
        }
        return ranges;
    }

    @Override
    public String toString(){
        StringBuffer buff = new StringBuffer();
        for(Matcher operand: operands){
            String msg = operand._toString();
            if(buff.length()==0)
                msg = msg.substring(1, msg.length()-1);
            else
                buff.append("&&");
            buff.append(msg);
        }
        return '['+buff.toString()+']';
    }
}

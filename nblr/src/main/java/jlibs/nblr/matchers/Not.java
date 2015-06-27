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

import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class Not extends Matcher{
    public final Matcher delegate;

    public Not(Matcher delegate){
        this.delegate = delegate;
    }

    @Override
    public boolean hasCustomJavaCode(){
        return super.hasCustomJavaCode() || delegate.hasCustomJavaCode();
    }

    @Override
    protected String __javaCode(String variable){
        if(delegate instanceof Any){
            Any any = (Any)delegate;

            if(any.chars==null)
                return variable+"==-1";

            StringBuilder buff = new StringBuilder();
            for(int ch: any.chars){
                if(buff.length()>0)
                    buff.append(" && ");
                buff.append(variable).append("!=").append(toJava(ch));
            }
            return buff.toString();
        }else
            return "!("+delegate._javaCode(variable)+")";
    }

    public static int minValue = Character.MIN_VALUE;
    @Override
    public List<Range> ranges(){
        return Range.minus(Collections.singletonList(new Range(minValue, Character.MAX_CODE_POINT)), delegate.ranges());
    }

    @Override
    public String toString(){
        String msg = delegate._toString();
        msg = msg.substring(1, msg.length()-1);
        return "[^"+msg+"]";
    }
}

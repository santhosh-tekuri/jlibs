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

import jlibs.core.lang.StringUtil;
import jlibs.core.util.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public final class Any extends Matcher{
    public static final Any NEW_LINE = new Any("\r\n");
    
    public final int chars[];

    public Any(String str){
        if(str!=null && str.length()>0)
            chars = StringUtil.toCodePoints(str);
        else
            chars = null;
    }

    public Any(char ch){
        chars = new int[]{ ch };
    }

    public Any(){
        chars = null;
    }

    public Any(int... codePoints){
        if(codePoints.length==0)
            chars = null;
        else
            chars = codePoints;
    }

    @Override
    public boolean canInline(){
        return super.canInline() || chars==null || chars.length==1;
    }

    @Override
    protected String __javaCode(String variable){
        if(chars==null)
            return variable+"!=-1";

        StringBuilder buff = new StringBuilder();
        for(int ch: chars){
            if(buff.length()>0)
                buff.append(" || ");
            buff.append(variable).append("==").append(toJava(ch));
        }
        return buff.toString();
    }

    @Override
    public List<Range> ranges(){
        if(chars==null)
            return Collections.singletonList(new Range(Character.MIN_VALUE, Character.MAX_VALUE));
        else{
            List<Range> ranges = new ArrayList<Range>(chars.length);
            for(int ch: chars)
                ranges.add(new Range(ch, ch));
            return Range.union(ranges);
        }
    }

    @Override
    public String toString(){
        return '['+encode(chars)+']';
    }
}
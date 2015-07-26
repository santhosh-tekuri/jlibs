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

import java.util.Collections;
import java.util.List;

import static java.lang.Character.MAX_CODE_POINT;
import static java.lang.Character.MIN_SUPPLEMENTARY_CODE_POINT;

/**
 * @author Santhosh Kumar T
 */
public final class Range extends Matcher{
    public static final Range SUPPLIMENTAL = new Range(MIN_SUPPLEMENTARY_CODE_POINT, MAX_CODE_POINT);
    public static final Range NON_SUPPLIMENTAL = new Range(0, MIN_SUPPLEMENTARY_CODE_POINT-1);

    public final int from, to;

    public Range(String chars){
        int codePoints[] = StringUtil.toCodePoints(chars);
        from = codePoints[0];
        to = codePoints[2];
    }

    public Range(int from, int to){
        this.from = from;
        this.to = to;
        if(from>to)
            throw new IllegalArgumentException("invalid range: "+this);
    }

    @Override
    protected String __javaCode(String variable){
        return String.format("%s>=%s && %s<=%s", variable, toJava(from), variable, toJava(to));
    }

    public List<jlibs.core.util.Range> ranges(){
        return Collections.singletonList(new jlibs.core.util.Range(from, to)); 
    }

    @Override
    public String toString(){
        return '['+encode(from)+'-'+encode(to)+']';
    }
}

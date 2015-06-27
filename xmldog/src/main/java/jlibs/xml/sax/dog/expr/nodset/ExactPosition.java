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

package jlibs.xml.sax.dog.expr.nodset;

import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.sniff.Event;

/**
 * @author Santhosh Kumar T
 */
public final class ExactPosition extends Positional{
    public final int pos;

    public ExactPosition(int pos){
        super(DataType.BOOLEAN, true);
        this.pos = pos;
    }

    @Override
    public Object getResult(Event event){
        if(predicate==null)
            return event.positionTrackerStack.peekFirst().position==pos;
        else
            return super.getResult(event);
    }

    @Override
    protected Object translate(Double result){
        return result.intValue()==pos;
    }

    @Override
    public String toString(){
        return String.format("exact-position(%d)", pos);
    }
}

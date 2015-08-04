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

package jlibs.core.graph.sequences;

/**
 * @author Santhosh Kumar T
 */
public class TOCSequence extends AbstractSequence<String>{
    private int number;
    private int count;

    public TOCSequence(int number, int count){
        this.number = number;
        this.count = count;
    }

    /*-------------------------------------------------[ Advancing ]---------------------------------------------------*/

    private int index;

    @Override
    protected String findNext(){
        index++;
        return index<=count ? number+"."+index : null;
    }

    /*-------------------------------------------------[ Reuse ]---------------------------------------------------*/

    @Override
    public void reset(){
        super.reset();
        _reset();
    }

    private void _reset(){
        index = 0;
    }

    @Override
    public TOCSequence copy(){
        return new TOCSequence(number, count);
    }

    /*-------------------------------------------------[ Optimization ]---------------------------------------------------*/

    @Override
    public int length(){
        return count;
    }
}

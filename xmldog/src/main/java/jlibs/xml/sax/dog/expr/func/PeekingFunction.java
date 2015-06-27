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

package jlibs.xml.sax.dog.expr.func;

import jlibs.xml.sax.dog.DataType;

/**
 * PeekingFunction is a function which can evaluate its results before all its arguments are known.
 * Note that such prediction might be possible only in some cases.
 * For example:
 *      Any arthimetic operation whose one operand is infinity always returns infinity
 *
 * @author Santhosh Kumar T
 */
public abstract class PeekingFunction extends Function{
    protected PeekingFunction(String name, DataType resultType, boolean varArgs, DataType... memberTypes){
        super(name, resultType, varArgs, memberTypes);
    }

    protected PeekingFunction(String name, DataType resultType, boolean varArgs, int mandatory, DataType... memberTypes){
        super("", name, resultType, varArgs, mandatory, memberTypes);
    }

    /**
     * After each member result is evaluated, this method is called.
     * Returns result(non-null), if the function can be evaluated.
     */
    protected abstract Object onMemberResult(int index, Object result);
}

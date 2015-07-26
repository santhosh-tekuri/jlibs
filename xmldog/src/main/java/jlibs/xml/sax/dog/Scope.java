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

package jlibs.xml.sax.dog;

/**
 * This class contains constants to specify evaluation scope of an expression.
 * Scope tells you, how result of expression is affected.
 *
 * @see jlibs.xml.sax.dog.expr.Expression#scope()
 *
 * @author Santhosh Kumar T
 */
public interface Scope{
    /**
     * used for those expressions whose result doesn't depend
     * on input xml documents. for example:
     *      1+2-3       -> always evaluates to 0
     *      count(/*)   -> always evaluates to 1
     */
    public static final int GLOBAL = 0;

    /**
     * used for absolute expressions. their result depends on
     * the input xml document
     */
    public static final int DOCUMENT = 1;

    /**
     * used for relative expressions. their result depends on
     * the context node used for evaluation
     */
    public static final int LOCAL = 2;
}

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

import jlibs.core.lang.ArrayUtil;
import jlibs.xml.sax.dog.DataType;

/**
 * @author Santhosh Kumar T
 */
public abstract class Function{
    public final String namespace;
    public final String name;
    public final DataType resultType;
    public final DataType memberTypes[];
    public final boolean varArgs;

    /**
     * tells how many arguments from beginning are mandatory.
     * in case varArgs is :
     *      o false: varArgs==membterTypes.lenth
     *      o true: varArgs==memberTypes.length or varArgs==memberTypes.length-1 
     */
    public final int mandatory;

    protected Function(String name, DataType resultType, boolean varArgs, DataType... memberTypes){
        this("", name, resultType, varArgs, memberTypes.length, memberTypes);
    }

    protected Function(String namespace, String name, DataType resultType, boolean varArgs, DataType... memberTypes){
        this(namespace, name, resultType, varArgs, memberTypes.length, memberTypes);
    }

    protected Function(String namespace, String name, DataType resultType, boolean varArgs, int mandatory, DataType... memberTypes){
        this.namespace = namespace;
        this.name = name;
        this.resultType = resultType;
        this.memberTypes = memberTypes;
        this.varArgs = varArgs;
        this.mandatory = mandatory;
    }

    public final boolean canAccept(int noOfMembers){
        return noOfMembers>=mandatory && (varArgs || noOfMembers<=memberTypes.length);
    }

    public final DataType memberType(int i){
        return i<memberTypes.length ? memberTypes[i] : memberTypes[memberTypes.length-1];
    }

    public abstract Object evaluate(Object... args);

    private static final String operators[] = { "+", "-", "*", "div", "mod", "and", "or", "|", "=", "!=", ">", ">=", "<", "<=" };
    public final boolean isOperator(){
        return ArrayUtil.indexOf(operators, name)!=-1;
    }
}

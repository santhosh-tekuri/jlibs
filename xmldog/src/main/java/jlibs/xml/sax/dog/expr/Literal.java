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

package jlibs.xml.sax.dog.expr;

import jlibs.core.lang.ImpossibleException;
import jlibs.core.util.LongTreeMap;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.NodeItem;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.sniff.Event;

import java.util.List;

/**
 * Literal Expression knows its result statically.
 * Note the result of any type is supported.
 *
 * All non-literal global expressions are simplified
 * to literal expressions
 *
 * @author Santhosh Kumar T
 */
public final class Literal extends Expression{
    private Object literal;

    public Literal(Object literal, DataType dataType){
        super(Scope.GLOBAL, dataType);
        assert DataType.valueOf(literal)==dataType;
        this.literal = literal;
    }

    @SuppressWarnings({"unchecked"})
    public void rawResultRequired(){
        if(resultType==DataType.NODESET && literal instanceof List){
            assert ((List)literal).isEmpty();
            literal = new LongTreeMap();
        }
    }
    
    @Override
    public Object getResult(){
        return literal;
    }

    @Override
    public Object getResult(Event event){
        throw new ImpossibleException();
    }

    @Override
    public Expression simplify(){
        return this;
    }

    @Override
    public String toString(){
        if(resultType==DataType.STRING)
            return String.format("'%s'", literal);
        else
            return literal.toString();
    }
}

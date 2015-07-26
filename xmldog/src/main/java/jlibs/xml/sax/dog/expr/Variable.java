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

package jlibs.xml.sax.dog.expr;

import jlibs.core.lang.ImpossibleException;
import jlibs.xml.sax.dog.DataType;
import jlibs.xml.sax.dog.Scope;
import jlibs.xml.sax.dog.sniff.Event;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 * @author Santhosh Kumar T
 */
public final class Variable extends Expression{
    public final XPathVariableResolver variableResolver;
    public final QName qname;

    public Variable(XPathVariableResolver variableResolver, QName qname){
        super(Scope.DOCUMENT, DataType.PRIMITIVE);
        this.variableResolver = variableResolver;
        this.qname = qname;
    }

    @Override
    public Object getResult(){
        throw new ImpossibleException();
    }

    @Override
    public Object getResult(Event event){
        return variableResolver.resolveVariable(qname);
    }

    @Override
    public String toString(){
        return '$'+ qname.toString();
    }
}

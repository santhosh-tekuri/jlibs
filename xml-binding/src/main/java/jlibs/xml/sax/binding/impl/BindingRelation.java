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

package jlibs.xml.sax.binding.impl;

import javax.xml.namespace.QName;

/**
 * Encapsulates both binding and relation. This is used by Registry.
 *
 * @author Santhosh Kumar T
 */
public class BindingRelation{
    public QName qname;

    public int bindingState;
    public Binding binding;

    public int relationState;
    public Relation relation;

    public BindingRelation(QName qname, int bindingState, Binding binding, int relationState, Relation relation){
        this.qname = qname;
        this.bindingState = bindingState;
        this.binding = binding;
        this.relationState = relationState;
        this.relation = relation;
    }

    public static final BindingRelation DO_NOTHING = new BindingRelation(Registry.ANY, -1, Binding.DO_NOTHING, -1, Relation.DO_NOTHING);
}

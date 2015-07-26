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

package jlibs.swing.outline;

import jlibs.core.graph.Visitor;

/**
 * @author Santhosh Kumar T
 */
public class DefaultColumn implements Column{
    private String name;
    private Class clazz;
    private Visitor visitor;

    public DefaultColumn(String name, Class clazz, Visitor visitor){
        this.name = name;
        this.clazz = clazz;
        this.visitor = visitor;
    }

    @Override
    public String getColumnName(){
        return name;
    }

    @Override
    public Class getColumnClass(){
        return clazz;
    }

    @Override    
    @SuppressWarnings({"unchecked"})
    public Object getValueFor(Object obj){
        return visitor.visit(obj);
    }

    @Override
    public boolean isCellEditable(Object obj){
        return false;
    }

    @Override
    public void setValueFor(Object obj, Object value){
        throw new UnsupportedOperationException();
    }
}

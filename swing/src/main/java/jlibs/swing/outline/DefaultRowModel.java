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

import org.netbeans.swing.outline.RowModel;

/**
 * @author Santhosh Kumar T
 */
public class DefaultRowModel implements RowModel{
    private Column columns[];

    public DefaultRowModel(Column... columns){
        this.columns = columns;
    }

    @Override
    public int getColumnCount(){
        return columns.length;
    }

    @Override
    public Object getValueFor(Object obj, int i){
        return columns[i].getValueFor(obj);
    }

    @Override
    public Class getColumnClass(int i){
        return columns[i].getColumnClass();
    }

    @Override
    public boolean isCellEditable(Object obj, int i){
        return columns[i].isCellEditable(obj);
    }

    @Override
    public void setValueFor(Object obj, int i, Object value){
        columns[i].setValueFor(obj, value);
    }

    @Override
    public String getColumnName(int i){
        return columns[i].getColumnName();
    }
}
